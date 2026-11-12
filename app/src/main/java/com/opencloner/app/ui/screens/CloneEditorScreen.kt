package com.opencloner.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencloner.app.data.model.AppItem
import com.opencloner.app.data.model.CloneConfig
import com.opencloner.app.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneEditorScreen(
    app: AppItem,
    sourceApkPath: String,
    onBackClick: () -> Unit,
    onStartBuild: (CloneConfig) -> Unit
) {
    val config = remember {
        CloneConfig(
            sourcePackageName = app.packageName,
            sourceAppName = app.appName,
            sourceApkPath = sourceApkPath,
            splitApkPaths = app.splitApkPaths
        )
    }

    // 1. Identity & Icon
    var appName by remember { mutableStateOf(config.customAppName) }
    var cloneNumber by remember { mutableStateOf(config.cloneNumber) }
    var hueShift by remember { mutableStateOf(config.iconHueShift) }
    var rotateDegrees by remember { mutableStateOf(config.iconRotateDegrees) }
    var flipHorizontal by remember { mutableStateOf(config.iconFlipHorizontal) }
    var badgeText by remember { mutableStateOf(config.badgeText) }
    var chameleonDisguise by remember { mutableStateOf(config.chameleonDisguise) }

    // 2. Privacy & Security
    var passwordProtect by remember { mutableStateOf(config.passwordProtect) }
    var appPassword by remember { mutableStateOf(config.appPassword) }
    var fakeCalculatorLock by remember { mutableStateOf(config.fakeCalculatorLock) }
    var fakeCrashOnStartup by remember { mutableStateOf(config.fakeCrashOnStartup) }
    var preventScreenshots by remember { mutableStateOf(config.preventScreenshots) }
    var hideFromRecents by remember { mutableStateOf(config.hideFromRecents) }
    var hideRoot by remember { mutableStateOf(config.hideRoot) }
    var hideDeveloperMode by remember { mutableStateOf(config.hideDeveloperMode) }

    // 3. Identity & Spoofing
    var changeAndroidId by remember { mutableStateOf(config.changeAndroidId) }
    var customAndroidId by remember { mutableStateOf(config.customAndroidId) }
    var randomizeBuildProps by remember { mutableStateOf(config.randomizeBuildProps) }
    var spoofDeviceModel by remember { mutableStateOf(config.spoofDeviceModel) }
    var mockWifiMac by remember { mutableStateOf(config.mockWifiMac) }

    // 4. Networking & Proxy
    var enableProxy by remember { mutableStateOf(config.enableProxy) }
    var proxyType by remember { mutableStateOf(config.proxyType) }
    var proxyHost by remember { mutableStateOf(config.proxyHost) }
    var proxyPort by remember { mutableStateOf(config.proxyPort.toString()) }
    var offlineMode by remember { mutableStateOf(config.offlineMode) }

    // 5. Location & Mock GPS
    var mockLocation by remember { mutableStateOf(config.mockLocation) }
    var mockLat by remember { mutableStateOf(config.mockLatitude.toString()) }
    var mockLng by remember { mutableStateOf(config.mockLongitude.toString()) }
    var hideMockLocation by remember { mutableStateOf(config.hideMockLocation) }

    // 6. Display & UI
    var forceDarkMode by remember { mutableStateOf(config.forceDarkMode) }
    var forceOrientation by remember { mutableStateOf(config.forceOrientation) }
    var immersiveMode by remember { mutableStateOf(config.immersiveMode) }
    var keepScreenOn by remember { mutableStateOf(config.keepScreenOn) }

    // 7. Media & Camera
    var fakeCamera by remember { mutableStateOf(config.fakeCamera) }
    var muteAudio by remember { mutableStateOf(config.muteAudioOnStart) }

    // 8. Storage & Sandboxing
    var isolateExternalStorage by remember { mutableStateOf(config.isolateExternalStorage) }
    var autoClearCacheOnExit by remember { mutableStateOf(config.autoClearCacheOnExit) }

    // 9. Launching & Automation
    var autoStartOnBoot by remember { mutableStateOf(config.autoStartOnBoot) }
    var secretDialerCode by remember { mutableStateOf(config.secretDialerCode) }

    // 10. Notifications
    var blockNotifications by remember { mutableStateOf(config.blockNotifications) }
    var notificationKeywordFilter by remember { mutableStateOf(config.notificationKeywordFilter) }

    // 11. Game & Performance
    var fpsLimiter by remember { mutableStateOf(config.fpsLimiter) }
    var touchVisualizer by remember { mutableStateOf(config.touchVisualizer) }
    var requestLargeHeap by remember { mutableStateOf(config.requestLargeHeap) }

    fun submitBuild() {
        config.customAppName = appName
        config.cloneNumber = cloneNumber
        config.updatePackageName()
        config.iconHueShift = hueShift
        config.iconRotateDegrees = rotateDegrees
        config.iconFlipHorizontal = flipHorizontal
        config.badgeText = badgeText
        config.chameleonDisguise = chameleonDisguise

        config.passwordProtect = passwordProtect
        config.appPassword = appPassword
        config.fakeCalculatorLock = fakeCalculatorLock
        config.fakeCrashOnStartup = fakeCrashOnStartup
        config.preventScreenshots = preventScreenshots
        config.hideFromRecents = hideFromRecents
        config.hideRoot = hideRoot
        config.hideDeveloperMode = hideDeveloperMode

        config.changeAndroidId = changeAndroidId
        config.customAndroidId = customAndroidId
        config.randomizeBuildProps = randomizeBuildProps
        config.spoofDeviceModel = spoofDeviceModel
        config.mockWifiMac = mockWifiMac

        config.enableProxy = enableProxy
        config.proxyType = proxyType
        config.proxyHost = proxyHost
        config.proxyPort = proxyPort.toIntOrNull() ?: 1080
        config.offlineMode = offlineMode

        config.mockLocation = mockLocation
        config.mockLatitude = mockLat.toDoubleOrNull() ?: 37.7749
        config.mockLongitude = mockLng.toDoubleOrNull() ?: -122.4194
        config.hideMockLocation = hideMockLocation

        config.forceDarkMode = forceDarkMode
        config.forceOrientation = forceOrientation
        config.immersiveMode = immersiveMode
        config.keepScreenOn = keepScreenOn

        config.fakeCamera = fakeCamera
        config.muteAudioOnStart = muteAudio

        config.isolateExternalStorage = isolateExternalStorage
        config.autoClearCacheOnExit = autoClearCacheOnExit

        config.autoStartOnBoot = autoStartOnBoot
        config.secretDialerCode = secretDialerCode

        config.blockNotifications = blockNotifications
        config.notificationKeywordFilter = notificationKeywordFilter

        config.fpsLimiter = fpsLimiter
        config.touchVisualizer = touchVisualizer
        config.requestLargeHeap = requestLargeHeap

        onStartBuild(config)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Clone", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { submitBuild() },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Build APK", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Live Icon & Title Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceElevated)
                                .rotate(rotateDegrees)
                                .scale(scaleX = if (flipHorizontal) -1f else 1f, scaleY = 1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (hueShift > 0f) Color(android.graphics.Color.HSVToColor(floatArrayOf(hueShift, 0.85f, 0.95f))) else CyanAccent
                            )
                            if (badgeText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(IndigoPrimary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(badgeText, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${app.packageName}.clone$cloneNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                            if (config.splitApkPaths.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Split APK Merging Active (${config.splitApkPaths.size} splits detected)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 1. App Identity & Icon Modding
            item {
                FeatureGroupCard(title = "App Identity & Icon Modding", icon = Icons.Default.Palette) {
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Clone Number", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Package suffix: .clone$cloneNumber", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (cloneNumber > 1) cloneNumber-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = CyanAccent)
                            }
                            Text("$cloneNumber", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                            IconButton(onClick = { cloneNumber++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = CyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Icon Hue Tint: ${hueShift.toInt()}°", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Slider(
                        value = hueShift,
                        onValueChange = { hueShift = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = IndigoPrimary, activeTrackColor = IndigoPrimary)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { rotateDegrees = (rotateDegrees + 90f) % 360f },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rotate ${rotateDegrees.toInt()}°")
                        }
                        OutlinedButton(
                            onClick = { flipHorizontal = !flipHorizontal },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (flipHorizontal) "Flipped" else "Flip")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("Badge Label (e.g. 2, Work, VIP)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Chameleon Icon Disguise:", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("None", "Calculator", "Clock", "Weather").forEach { disguise ->
                            FilterChip(
                                selected = (chameleonDisguise.ifBlank { "None" }) == disguise,
                                onClick = { chameleonDisguise = if (disguise == "None") "" else disguise },
                                label = { Text(disguise, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // 2. Privacy & Security Options
            item {
                FeatureGroupCard(title = "Privacy & Security Options", icon = Icons.Default.Lock) {
                    ToggleRow(
                        title = "Password Protect App",
                        subtitle = "Require PIN or password before app opens",
                        checked = passwordProtect,
                        onCheckedChange = { passwordProtect = it }
                    )
                    AnimatedVisibility(visible = passwordProtect) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = appPassword,
                                onValueChange = { appPassword = it },
                                label = { Text("Enter App Password/PIN") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customFieldColors()
                            )
                        }
                    }
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Fake Calculator Disguise",
                        subtitle = "Opens as a working calculator; secret PIN unlocks clone",
                        checked = fakeCalculatorLock,
                        onCheckedChange = { fakeCalculatorLock = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Fake Crash Dialog on Launch",
                        subtitle = "Shows 'Unfortunately app has stopped' to mislead snoopers",
                        checked = fakeCrashOnStartup,
                        onCheckedChange = { fakeCrashOnStartup = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Prevent Screenshots (FLAG_SECURE)",
                        subtitle = "Blocks screen capture and hides app preview in recents",
                        checked = preventScreenshots,
                        onCheckedChange = { preventScreenshots = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Stealth Mode (Hide from Recents)",
                        subtitle = "Excludes cloned app from recent apps switcher",
                        checked = hideFromRecents,
                        onCheckedChange = { hideFromRecents = it }
                    )
                }
            }

            // 3. Identity & Device Spoofing
            item {
                FeatureGroupCard(title = "Identity & Device Spoofing", icon = Icons.Default.Fingerprint) {
                    ToggleRow(
                        title = "Change Android ID",
                        subtitle = "Overrides system 64-bit Android ID",
                        checked = changeAndroidId,
                        onCheckedChange = {
                            changeAndroidId = it
                            if (it && customAndroidId.isBlank()) {
                                customAndroidId = UUID.randomUUID().toString().replace("-", "").take(16)
                            }
                        }
                    )
                    AnimatedVisibility(visible = changeAndroidId) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customAndroidId,
                                onValueChange = { customAndroidId = it },
                                label = { Text("Custom Android ID (Hex)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = customFieldColors()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                customAndroidId = UUID.randomUUID().toString().replace("-", "").take(16)
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Randomize", tint = CyanAccent)
                            }
                        }
                    }
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Spoof Device Build Props",
                        subtitle = "Spoofs device brand, model, and hardware fingerprint",
                        checked = randomizeBuildProps,
                        onCheckedChange = { randomizeBuildProps = it }
                    )
                    AnimatedVisibility(visible = randomizeBuildProps) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text("Device Model Presets:", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Galaxy S24", "Pixel 8 Pro", "Xiaomi 14", "OnePlus 12").forEach { dev ->
                                    FilterChip(
                                        selected = spoofDeviceModel == dev,
                                        onClick = { spoofDeviceModel = dev },
                                        label = { Text(dev, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Randomize Wi-Fi MAC Address",
                        subtitle = "Spoofs hardware MAC address using authentic vendor OUI database",
                        checked = mockWifiMac,
                        onCheckedChange = { mockWifiMac = it }
                    )
                }
            }

            // 4. Networking & Proxy Options
            item {
                FeatureGroupCard(title = "Networking & Proxy Options", icon = Icons.Default.VpnLock) {
                    ToggleRow(
                        title = "Route through Proxy",
                        subtitle = "Send all app traffic via SOCKS5 or HTTP proxy",
                        checked = enableProxy,
                        onCheckedChange = { enableProxy = it }
                    )
                    AnimatedVisibility(visible = enableProxy) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = proxyType == "SOCKS5",
                                    onClick = { proxyType = "SOCKS5" },
                                    label = { Text("SOCKS5 Proxy") }
                                )
                                FilterChip(
                                    selected = proxyType == "HTTP",
                                    onClick = { proxyType = "HTTP" },
                                    label = { Text("HTTP Proxy") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = proxyHost,
                                    onValueChange = { proxyHost = it },
                                    label = { Text("Proxy Host/IP") },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                    colors = customFieldColors()
                                )
                                OutlinedTextField(
                                    value = proxyPort,
                                    onValueChange = { proxyPort = it },
                                    label = { Text("Port") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = customFieldColors()
                                )
                            }
                        }
                    }
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Offline Mode (Kill Switch)",
                        subtitle = "Completely disable all internet permissions for this clone",
                        checked = offlineMode,
                        onCheckedChange = { offlineMode = it }
                    )
                }
            }

            // 5. Location & Mock GPS Options
            item {
                FeatureGroupCard(title = "Location & Mock GPS", icon = Icons.Default.LocationOn) {
                    ToggleRow(
                        title = "Fake GPS Location",
                        subtitle = "Inject custom GPS coordinates directly into location provider",
                        checked = mockLocation,
                        onCheckedChange = { mockLocation = it }
                    )
                    AnimatedVisibility(visible = mockLocation) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text("Quick City Presets (from cities.csv):", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = mockLat == "35.6897",
                                    onClick = { mockLat = "35.6897"; mockLng = "139.6922" },
                                    label = { Text("Tokyo", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = mockLat == "40.7128",
                                    onClick = { mockLat = "40.7128"; mockLng = "-74.0060" },
                                    label = { Text("New York", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = mockLat == "51.5074",
                                    onClick = { mockLat = "51.5074"; mockLng = "-0.1278" },
                                    label = { Text("London", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = mockLat == "25.2048",
                                    onClick = { mockLat = "25.2048"; mockLng = "55.2708" },
                                    label = { Text("Dubai", fontSize = 11.sp) }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = mockLat,
                                    onValueChange = { mockLat = it },
                                    label = { Text("Latitude") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = customFieldColors()
                                )
                                OutlinedTextField(
                                    value = mockLng,
                                    onValueChange = { mockLng = it },
                                    label = { Text("Longitude") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = customFieldColors()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleRow(
                                title = "Hide Mock Location",
                                subtitle = "Prevents target app from detecting isFromMockProvider",
                                checked = hideMockLocation,
                                onCheckedChange = { hideMockLocation = it }
                            )
                        }
                    }
                }
            }

            // 6. Display & UI Controls
            item {
                FeatureGroupCard(title = "Display & UI Controls", icon = Icons.Default.Tv) {
                    ToggleRow(
                        title = "Force Dark Mode",
                        subtitle = "Inverts surfaces and text to eye-friendly dark colors",
                        checked = forceDarkMode,
                        onCheckedChange = { forceDarkMode = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Immersive Full-Screen Mode",
                        subtitle = "Hides Android status bar and navigation bar",
                        checked = immersiveMode,
                        onCheckedChange = { immersiveMode = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Keep Screen On",
                        subtitle = "Prevents device display from sleeping while app is open",
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it }
                    )
                }
            }

            // 7. Media & Camera Emulation
            item {
                FeatureGroupCard(title = "Media & Hardware Emulation", icon = Icons.Default.Videocam) {
                    ToggleRow(
                        title = "Fake Camera Feed",
                        subtitle = "Intercept camera capture intent and supply a gallery photo",
                        checked = fakeCamera,
                        onCheckedChange = { fakeCamera = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Mute Volume on Launch",
                        subtitle = "Silences all media playback when the clone starts",
                        checked = muteAudio,
                        onCheckedChange = { muteAudio = it }
                    )
                }
            }

            // 8. Storage & Sandboxing
            item {
                FeatureGroupCard(title = "Storage & Sandboxing Options", icon = Icons.Default.Folder) {
                    ToggleRow(
                        title = "Isolate External Storage",
                        subtitle = "Redirect shared storage to app-isolated private folder",
                        checked = isolateExternalStorage,
                        onCheckedChange = { isolateExternalStorage = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Auto-Clear Cache on Exit",
                        subtitle = "Deletes temporary cache and logs every time the app closes",
                        checked = autoClearCacheOnExit,
                        onCheckedChange = { autoClearCacheOnExit = it }
                    )
                }
            }

            // 9. Launching & Automation
            item {
                FeatureGroupCard(title = "Launching & Automation", icon = Icons.Default.RocketLaunch) {
                    ToggleRow(
                        title = "Auto-Start on Device Boot",
                        subtitle = "Launches clone automatically when Android starts up",
                        checked = autoStartOnBoot,
                        onCheckedChange = { autoStartOnBoot = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    OutlinedTextField(
                        value = secretDialerCode,
                        onValueChange = { secretDialerCode = it },
                        label = { Text("Secret Dialer Code (e.g. *#*#1234#*#*)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customFieldColors()
                    )
                }
            }

            // 10. Game & Performance
            item {
                FeatureGroupCard(title = "Game & Performance Options", icon = Icons.Default.SportsEsports) {
                    ToggleRow(
                        title = "Request Large Heap (RAM)",
                        subtitle = "Allocates extra memory heap to prevent game lag & OOM crashes",
                        checked = requestLargeHeap,
                        onCheckedChange = { requestLargeHeap = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Touch Pointer Visualizer",
                        subtitle = "Displays circular visual touch feedback on screen",
                        checked = touchVisualizer,
                        onCheckedChange = { touchVisualizer = it }
                    )
                }
            }

            // 11. Developer & Anti-Detection
            item {
                FeatureGroupCard(title = "Anti-Detection & Safe Mode", icon = Icons.Default.Security) {
                    ToggleRow(
                        title = "Hide Root & Magisk",
                        subtitle = "Conceals root binaries, su files, and test-keys from target app",
                        checked = hideRoot,
                        onCheckedChange = { hideRoot = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Hide Developer Mode & USB Debugging",
                        subtitle = "Conceals developer settings to bypass banking/anti-cheat checks",
                        checked = hideDeveloperMode,
                        onCheckedChange = { hideDeveloperMode = it }
                    )
                }
            }

            // 12. Notifications
            item {
                FeatureGroupCard(title = "Notification Controls", icon = Icons.Default.Notifications) {
                    ToggleRow(
                        title = "Block All Notifications",
                        subtitle = "Completely silences all notification posts from this clone",
                        checked = blockNotifications,
                        onCheckedChange = { blockNotifications = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    OutlinedTextField(
                        value = notificationKeywordFilter,
                        onValueChange = { notificationKeywordFilter = it },
                        label = { Text("Keyword Blacklist (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customFieldColors()
                    )
                }
            }

            // 🚀 Action Button
            item {
                Button(
                    onClick = { submitBuild() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Clone & Install APK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun FeatureGroupCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IndigoPrimary,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

@Composable
fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = IndigoPrimary,
    unfocusedBorderColor = DarkSurfaceElevated,
    focusedContainerColor = DarkBackground,
    unfocusedContainerColor = DarkBackground,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
