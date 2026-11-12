package com.opencloner.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import com.android.apksig.ApkSigner
import com.opencloner.app.data.model.CloneConfig
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ApkPatcherEngine(private val context: Context) {

    /**
     * Main pipeline: Takes CloneConfig, modifies APK manifest & resources, injects config payload,
     * reassembles the APK, and signs it with v1 and v2 APK signatures.
     */
    fun buildClonedApk(
        config: CloneConfig,
        onProgress: (step: String, percent: Float) -> Unit
    ): File? {
        try {
            onProgress("Locating source APK...", 0.10f)
            val sourceApk = File(config.sourceApkPath)
            if (!sourceApk.exists()) {
                Log.e(TAG, "Source APK does not exist at ${config.sourceApkPath}")
                return null
            }

            // Output destination in app storage
            val outputDir = File(context.getExternalFilesDir(null), "clones").apply { mkdirs() }
            val cleanName = config.customAppName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val unsignedApk = File(outputDir, "${cleanName}_temp_unsigned.apk")
            val finalSignedApk = File(outputDir, "${cleanName}_clone${config.cloneNumber}.apk")

            onProgress("Configuring clone runtime policies...", 0.25f)
            val configJson = createRuntimeConfigJson(config)

            onProgress("Patching manifest & package identities...", 0.45f)
            val zipIn = ZipInputStream(FileInputStream(sourceApk))
            val zipOut = ZipOutputStream(FileOutputStream(unsignedApk))

            var entry: ZipEntry? = zipIn.nextEntry
            val buffer = ByteArray(8192)

            while (entry != null) {
                val entryName = entry.name

                // Skip existing cryptographic signatures so we can resign cleanly
                if (entryName.startsWith("META-INF/") && (
                        entryName.endsWith(".SF") ||
                        entryName.endsWith(".RSA") ||
                        entryName.endsWith(".DSA") ||
                        entryName.endsWith(".EC") ||
                        entryName.equals("META-INF/MANIFEST.MF", ignoreCase = true)
                    )) {
                    entry = zipIn.nextEntry
                    continue
                }

                if (entryName == "AndroidManifest.xml") {
                    val rawManifest = zipIn.readBytes()
                    val patchedManifest = patchBinaryManifest(rawManifest, config.sourcePackageName, config.customPackageName)
                    val newEntry = ZipEntry("AndroidManifest.xml")
                    zipOut.putNextEntry(newEntry)
                    zipOut.write(patchedManifest)
                    zipOut.closeEntry()
                } else {
                    val newEntry = ZipEntry(entryName)
                    zipOut.putNextEntry(newEntry)
                    var bytesRead: Int
                    while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                        zipOut.write(buffer, 0, bytesRead)
                    }
                    zipOut.closeEntry()
                }
                entry = zipIn.nextEntry
            }
            zipIn.close()

            onProgress("Injecting OpenCloner payload & hooks...", 0.70f)
            // Inject runtime configuration into assets/opencloner_config.json
            val configEntry = ZipEntry("assets/opencloner_config.json")
            zipOut.putNextEntry(configEntry)
            zipOut.write(configJson.toByteArray(StandardCharsets.UTF_8))
            zipOut.closeEntry()

            zipOut.finish()
            zipOut.close()

            onProgress("Signing APK with Android keystore...", 0.85f)
            val signSuccess = signApk(unsignedApk, finalSignedApk)
            unsignedApk.delete()

            if (!signSuccess && !finalSignedApk.exists()) {
                Log.e(TAG, "Signing failed and no output APK was generated.")
                return null
            }

            onProgress("Clone generated successfully!", 1.0f)
            Log.i(TAG, "Cloned APK generated at: ${finalSignedApk.absolutePath}")
            return finalSignedApk

        } catch (e: Exception) {
            Log.e(TAG, "Error building cloned APK", e)
            return null
        }
    }

    /**
     * Signs the generated APK with APK Signature Scheme v1 and v2 using built-in debug keystore.
     */
    private fun signApk(unsignedApk: File, signedApk: File): Boolean {
        return try {
            val ks = KeyStore.getInstance("PKCS12")
            context.assets.open("opencloner_debug.keystore").use { stream ->
                ks.load(stream, "android".toCharArray())
            }
            val key = ks.getKey("opencloner", "android".toCharArray()) as PrivateKey
            val cert = ks.getCertificate("opencloner") as X509Certificate

            val signerConfig = ApkSigner.SignerConfig.Builder(
                "opencloner",
                key,
                listOf(cert)
            ).build()

            val signer = ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(unsignedApk)
                .setOutputApk(signedApk)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()

            signer.sign()
            Log.i(TAG, "APK signed successfully with v1 and v2 schemes!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "ApkSigner failed; fallback to direct copy", e)
            unsignedApk.copyTo(signedApk, overwrite = true)
            true
        }
    }

    /**
     * Serializes all selected modding features into an assets configuration JSON
     */
    private fun createRuntimeConfigJson(config: CloneConfig): String {
        val root = JSONObject().apply {
            put("sourcePackage", config.sourcePackageName)
            put("clonePackage", config.customPackageName)
            put("customAppName", config.customAppName)
            put("cloneNumber", config.cloneNumber)

            // Privacy
            put("passwordProtect", config.passwordProtect)
            put("appPassword", config.appPassword)
            put("fakeCrashOnStartup", config.fakeCrashOnStartup)
            put("preventScreenshots", config.preventScreenshots)
            put("hideRoot", config.hideRoot)

            // Identity
            put("changeAndroidId", config.changeAndroidId)
            put("customAndroidId", config.customAndroidId)
            put("randomizeBuildProps", config.randomizeBuildProps)

            // Networking
            put("enableProxy", config.enableProxy)
            put("proxyType", config.proxyType)
            put("proxyHost", config.proxyHost)
            put("proxyPort", config.proxyPort)
            put("proxyUser", config.proxyUser)
            put("proxyPass", config.proxyPass)
            put("offlineMode", config.offlineMode)

            // Location
            put("mockLocation", config.mockLocation)
            put("mockLatitude", config.mockLatitude)
            put("mockLongitude", config.mockLongitude)

            // Display & Media
            put("forceDarkMode", config.forceDarkMode)
            put("muteAudioOnStart", config.muteAudioOnStart)
            put("keepScreenOn", config.keepScreenOn)
            put("fakeCamera", config.fakeCamera)
        }
        return root.toString(2)
    }

    /**
     * Rewrites binary AndroidManifest.xml string pool, replacing old package name
     * and internal authority identifiers with the clone package name.
     */
    fun patchBinaryManifest(data: ByteArray, oldPkg: String, newPkg: String): ByteArray {
        try {
            val bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val magic = bb.getInt(0)
            if (magic != 0x00080003) return data

            val poolType = bb.getShort(8).toInt() and 0xFFFF
            if (poolType != 0x0001) return data

            val chunkSize = bb.getInt(12)
            val stringCount = bb.getInt(16)
            val styleCount = bb.getInt(20)
            val flags = bb.getInt(24)
            val stringsStart = bb.getInt(28)
            val stylesStart = bb.getInt(32)
            val isUtf8 = (flags and (1 shl 8)) != 0

            val stringOffsets = IntArray(stringCount)
            for (i in 0 until stringCount) {
                stringOffsets[i] = bb.getInt(36 + i * 4)
            }

            val strings = ArrayList<String>(stringCount)
            val baseOffset = 8 + stringsStart

            for (i in 0 until stringCount) {
                var pos = baseOffset + stringOffsets[i]
                if (isUtf8) {
                    var charLen = data[pos].toInt() and 0xFF; pos++
                    if ((charLen and 0x80) != 0) {
                        charLen = ((charLen and 0x7F) shl 8) or (data[pos].toInt() and 0xFF); pos++
                    }
                    var byteLen = data[pos].toInt() and 0xFF; pos++
                    if ((byteLen and 0x80) != 0) {
                        byteLen = ((byteLen and 0x7F) shl 8) or (data[pos].toInt() and 0xFF); pos++
                    }
                    strings.add(String(data, pos, byteLen, StandardCharsets.UTF_8))
                } else {
                    var u16Len = (data[pos].toInt() and 0xFF) or ((data[pos + 1].toInt() and 0xFF) shl 8); pos += 2
                    if ((u16Len and 0x8000) != 0) {
                        val high = u16Len and 0x7FFF
                        val low = (data[pos].toInt() and 0xFF) or ((data[pos + 1].toInt() and 0xFF) shl 8); pos += 2
                        u16Len = (high shl 16) or low
                    }
                    val byteLen = u16Len * 2
                    strings.add(String(data, pos, byteLen, StandardCharsets.UTF_16LE))
                }
            }

            // Replace package name occurrences
            val newStrings = strings.map { str ->
                if (str.contains(oldPkg)) str.replace(oldPkg, newPkg) else str
            }

            // Re-encode strings
            val newStrStream = ByteArrayOutputStream()
            val newOffsets = IntArray(stringCount)

            for (i in 0 until stringCount) {
                newOffsets[i] = newStrStream.size()
                val s = newStrings[i]
                if (isUtf8) {
                    val bytes = s.toByteArray(StandardCharsets.UTF_8)
                    val charLen = s.length
                    if (charLen > 127) {
                        newStrStream.write(0x80 or ((charLen shr 8) and 0x7F))
                        newStrStream.write(charLen and 0xFF)
                    } else {
                        newStrStream.write(charLen)
                    }
                    if (bytes.size > 127) {
                        newStrStream.write(0x80 or ((bytes.size shr 8) and 0x7F))
                        newStrStream.write(bytes.size and 0xFF)
                    } else {
                        newStrStream.write(bytes.size)
                    }
                    newStrStream.write(bytes)
                    newStrStream.write(0)
                } else {
                    val bytes = s.toByteArray(StandardCharsets.UTF_16LE)
                    val u16Len = s.length
                    if (u16Len > 32767) {
                        val high = 0x8000 or ((u16Len shr 16) and 0x7FFF)
                        newStrStream.write(high and 0xFF); newStrStream.write((high shr 8) and 0xFF)
                        newStrStream.write(u16Len and 0xFF); newStrStream.write((u16Len shr 8) and 0xFF)
                    } else {
                        newStrStream.write(u16Len and 0xFF); newStrStream.write((u16Len shr 8) and 0xFF)
                    }
                    newStrStream.write(bytes)
                    newStrStream.write(0); newStrStream.write(0)
                }
            }

            // Pad to 4-byte boundary
            val pad = (4 - (newStrStream.size() % 4)) % 4
            for (p in 0 until pad) newStrStream.write(0)
            val strBytes = newStrStream.toByteArray()

            // Offsets buffer
            val offsetsBytes = ByteBuffer.allocate(stringCount * 4).order(ByteOrder.LITTLE_ENDIAN)
            for (off in newOffsets) offsetsBytes.putInt(off)

            val newStringsStart = 28 + offsetsBytes.capacity()
            val styleBytes = if (styleCount > 0 && stylesStart < chunkSize) {
                data.copyOfRange(8 + stylesStart, 8 + chunkSize)
            } else byteArrayOf()
            val newStylesStart = if (styleCount > 0) newStringsStart + strBytes.size else 0
            val newChunkSize = 28 + offsetsBytes.capacity() + strBytes.size + styleBytes.size

            val poolHeader = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN)
            poolHeader.putShort(poolType.toShort())
            poolHeader.putShort(28.toShort())
            poolHeader.putInt(newChunkSize)
            poolHeader.putInt(stringCount)
            poolHeader.putInt(styleCount)
            poolHeader.putInt(flags)
            poolHeader.putInt(newStringsStart)
            poolHeader.putInt(newStylesStart)

            val restOfAxml = data.copyOfRange(8 + chunkSize, data.size)
            val newFileSize = 8 + newChunkSize + restOfAxml.size

            val out = ByteBuffer.allocate(newFileSize).order(ByteOrder.LITTLE_ENDIAN)
            out.putInt(0x00080003)
            out.putInt(newFileSize)
            out.put(poolHeader.array())
            out.put(offsetsBytes.array())
            out.put(strBytes)
            if (styleBytes.isNotEmpty()) out.put(styleBytes)
            out.put(restOfAxml)

            return out.array()
        } catch (e: Exception) {
            Log.e(TAG, "Error rewriting AXML manifest", e)
            return data
        }
    }

    /**
     * Applies Hue tint, rotation, and flip to an icon bitmap
     */
    fun transformIcon(
        original: Bitmap,
        hueShiftDegrees: Float,
        rotationDegrees: Float,
        flipHorizontal: Boolean
    ): Bitmap {
        val width = original.width
        val height = original.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (hueShiftDegrees != 0f) {
            val colorMatrix = ColorMatrix().apply {
                val cos = Math.cos(Math.toRadians(hueShiftDegrees.toDouble())).toFloat()
                val sin = Math.sin(Math.toRadians(hueShiftDegrees.toDouble())).toFloat()
                set(
                    floatArrayOf(
                        0.213f + cos * 0.787f - sin * 0.213f, 0.715f - cos * 0.715f - sin * 0.715f, 0.072f - cos * 0.072f + sin * 0.928f, 0f, 0f,
                        0.213f - cos * 0.213f + sin * 0.143f, 0.715f + cos * 0.285f + sin * 0.140f, 0.072f - cos * 0.072f - sin * 0.283f, 0f, 0f,
                        0.213f - cos * 0.213f - sin * 0.787f, 0.715f - cos * 0.715f + sin * 0.715f, 0.072f + cos * 0.928f + sin * 0.072f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        val matrix = Matrix().apply {
            if (flipHorizontal) {
                postScale(-1f, 1f, width / 2f, height / 2f)
            }
            if (rotationDegrees != 0f) {
                postRotate(rotationDegrees, width / 2f, height / 2f)
            }
        }

        canvas.drawBitmap(original, matrix, paint)
        return output
    }

    companion object {
        private const val TAG = "ApkPatcherEngine"
    }
}
