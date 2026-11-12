package com.opencloner.app.engine

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import com.opencloner.app.data.model.AppItem
import com.opencloner.app.data.model.PresetType
import com.opencloner.app.data.model.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkProfileManager private constructor(private val context: Context) {

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val adminComponent = ComponentName(context, CloneAdminReceiver::class.java)

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Unprovisioned)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val workUser = getWorkProfileUser()
        if (workUser != null) {
            val clonedApps = getClonedApps()
            val isQuietMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                userManager.isQuietModeEnabled(workUser)
            } else {
                false
            }
            _profileState.value = ProfileState.Active(
                profileId = workUser.hashCode(),
                clonedCount = clonedApps.size,
                isPaused = isQuietMode
            )
        } else {
            _profileState.value = ProfileState.Unprovisioned
        }
    }

    /**
     * Finds the managed profile UserHandle, if one exists.
     */
    fun getWorkProfileUser(): UserHandle? {
        val myUserHandle = Process.myUserHandle()
        for (profile in userManager.userProfiles) {
            if (profile != myUserHandle) {
                return profile
            }
        }
        return null
    }

    fun isProfileOwner(): Boolean {
        return dpm.isProfileOwnerApp(context.packageName)
    }

    /**
     * Constructs the intent to trigger system Work Profile provisioning.
     */
    fun createProvisioningIntent(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                adminComponent
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true)
            }
        }
        return intent
    }

    /**
     * Called when profile is newly created & configured.
     */
    fun onProfileCreated(context: Context) {
        try {
            // Enable OpenCloner inside the profile
            dpm.setProfileEnabled(adminComponent)
            dpm.setProfileName(adminComponent, "OpenCloner Sandbox")

            // Allow cross-profile intent resolution (e.g. sharing, browser, media)
            val intentFilter = Intent(Intent.ACTION_SEND).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                type = "*/*"
            }
            dpm.addCrossProfileIntentFilter(
                adminComponent,
                android.content.IntentFilter(Intent.ACTION_SEND).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    try {
                        addDataType("*/*")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding cross-profile data type", e)
                    }
                },
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED or DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
            )

            Log.i(TAG, "Profile configured successfully.")
            refreshState()
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring new profile", e)
        }
    }

    /**
     * Clones an installed app into the sandbox using DevicePolicyManager.installExistingPackage.
     * This avoids downloading or re-signing, preserving original signatures and anti-cheat compliance.
     */
    fun cloneApp(packageName: String, preset: PresetType): Boolean {
        return try {
            if (isProfileOwner()) {
                val success = dpm.installExistingPackage(adminComponent, packageName)
                Log.i(TAG, "Installed package $packageName into sandbox: $success")
                refreshState()
                success
            } else {
                Log.w(TAG, "App is not profile owner yet; requesting elevated Shizuku fallback.")
                ShizukuBridge.installExistingPackage(packageName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clone app $packageName", e)
            false
        }
    }

    /**
     * Launches a cloned app (checks both standalone installed clones and work profile sandbox).
     */
    fun launchClonedApp(packageName: String): Boolean {
        val pm = context.packageManager
        // 1. Try launching standalone cloned APK on primary user
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }

        // 2. Try launching inside work profile
        val workUser = getWorkProfileUser() ?: return false
        val activities = launcherApps.getActivityList(packageName, workUser)
        return if (activities.isNotEmpty()) {
            val component = activities[0].componentName
            launcherApps.startMainActivity(component, workUser, null, null)
            true
        } else {
            Log.w(TAG, "Could not find launcher activity for $packageName.")
            false
        }
    }

    /**
     * Returns a list of apps currently installed in the host (main) profile.
     */
    fun getInstalledHostApps(): List<AppItem> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val result = mutableListOf<AppItem>()
        val clonedSet = getClonedApps().map { it.packageName }.toSet()

        for (pkg in packages) {
            val isSystem = (pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = pkg.applicationInfo.loadLabel(pm).toString()
            val icon = pkg.applicationInfo.loadIcon(pm)

            // Filter out internal system stubs, but keep user apps & major services
            if (!isSystem || pkg.packageName in listOf("com.whatsapp", "org.telegram.messenger", "com.discord")) {
                result.add(
                    AppItem(
                        packageName = pkg.packageName,
                        appName = appName,
                        icon = icon,
                        isSystemApp = isSystem,
                        isCloned = clonedSet.contains(pkg.packageName),
                        sourceApkPath = pkg.applicationInfo.sourceDir ?: ""
                    )
                )
            }
        }
        return result.sortedBy { it.appName.lowercase() }
    }

    /**
     * Returns all applications running inside standalone APK clones and work profile.
     */
    fun getClonedApps(): List<AppItem> {
        val result = mutableListOf<AppItem>()
        val pm = context.packageManager

        // 1. Standalone clones on the primary user space
        try {
            val installed = pm.getInstalledPackages(0)
            for (pkg in installed) {
                if (pkg.packageName.contains(".clone") || pkg.packageName.endsWith(".cloned")) {
                    val appName = pkg.applicationInfo.loadLabel(pm).toString()
                    val icon = pkg.applicationInfo.loadIcon(pm)
                    result.add(
                        AppItem(
                            packageName = pkg.packageName,
                            appName = appName,
                            icon = icon,
                            isSystemApp = false,
                            isCloned = true,
                            sourceApkPath = pkg.applicationInfo.sourceDir ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying installed standalone clones", e)
        }

        // 2. Work profile clones
        val workUser = getWorkProfileUser()
        if (workUser != null) {
            try {
                val activities = launcherApps.getActivityList(null, workUser)
                for (activityInfo in activities) {
                    val appInfo = activityInfo.applicationInfo
                    if (result.none { it.packageName == appInfo.packageName }) {
                        result.add(
                            AppItem(
                                packageName = appInfo.packageName,
                                appName = activityInfo.label.toString(),
                                icon = activityInfo.getBadgedIcon(0),
                                isCloned = true,
                                sourceApkPath = appInfo.sourceDir ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying work profile apps", e)
            }
        }

        return result
    }

    /**
     * Freezes/pauses the sandbox profile to immediately halt all background processes and save battery.
     */
    fun setSandboxPaused(paused: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val workUser = getWorkProfileUser() ?: return false
            return try {
                userManager.requestQuietModeEnabled(paused, workUser)
                refreshState()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle quiet mode", e)
                false
            }
        }
        return false
    }

    companion object {
        private const val TAG = "WorkProfileManager"
        @Volatile
        private var instance: WorkProfileManager? = null

        fun getInstance(context: Context): WorkProfileManager {
            return instance ?: synchronized(this) {
                instance ?: WorkProfileManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
