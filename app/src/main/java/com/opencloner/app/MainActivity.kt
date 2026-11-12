package com.opencloner.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.opencloner.app.data.model.AppItem
import com.opencloner.app.data.model.CloneConfig
import com.opencloner.app.engine.ApkPatcherEngine
import com.opencloner.app.engine.ShizukuBridge
import com.opencloner.app.engine.WorkProfileManager
import com.opencloner.app.ui.screens.*
import com.opencloner.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ScreenTab(val title: String, val icon: ImageVector) {
    HOME("Clones", Icons.Default.Layers),
    PICKER("Clone App", Icons.Default.AddCircle),
    PRESETS("Presets", Icons.Default.Tune),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var profileManager: WorkProfileManager
    private lateinit var patcherEngine: ApkPatcherEngine

    private val provisioningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Sandbox Container configured successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Provisioning completed or running in background.", Toast.LENGTH_SHORT).show()
        }
        profileManager.refreshState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileManager = WorkProfileManager.getInstance(this)
        patcherEngine = ApkPatcherEngine(this)

        setContent {
            OpenClonerTheme {
                MainAppScaffold()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        profileManager.refreshState()
    }

    @Composable
    private fun MainAppScaffold() {
        var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
        val profileState by profileManager.profileState.collectAsState()
        var clonedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
        var hostApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

        // State for custom clone editor
        var selectedAppToConfigure by remember { mutableStateOf<AppItem?>(null) }
        var isBuildingApk by remember { mutableStateOf(false) }
        var buildStatusText by remember { mutableStateOf("") }
        var buildProgress by remember { mutableStateOf(0f) }
        var completedApkFile by remember { mutableStateOf<File?>(null) }

        // Refresh apps list when state changes or tab switches
        LaunchedEffect(profileState, currentTab) {
            withContext(Dispatchers.IO) {
                val cloned = profileManager.getClonedApps()
                val host = profileManager.getInstalledHostApps()
                withContext(Dispatchers.Main) {
                    clonedApps = cloned
                    hostApps = host
                }
            }
        }

        // If an app is being customized, show the full CloneEditorScreen
        if (selectedAppToConfigure != null) {
            val app = selectedAppToConfigure!!
            CloneEditorScreen(
                app = app,
                sourceApkPath = app.sourceApkPath,
                onBackClick = { selectedAppToConfigure = null },
                onStartBuild = { config ->
                    isBuildingApk = true
                    buildStatusText = "Initializing APK Patcher..."
                    buildProgress = 0.05f

                    lifecycleScope.launch(Dispatchers.IO) {
                        val outputApk = patcherEngine.buildClonedApk(config) { step, percent ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                buildStatusText = step
                                buildProgress = percent
                            }
                        }

                        withContext(Dispatchers.Main) {
                            isBuildingApk = false
                            if (outputApk != null && outputApk.exists()) {
                                completedApkFile = outputApk
                            } else {
                                Toast.makeText(this@MainActivity, "Failed to build APK. Check storage permission.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )

            // Building Progress Dialog
            if (isBuildingApk) {
                Dialog(onDismissRequest = {}) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                progress = { buildProgress },
                                color = IndigoPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Generating Cloned APK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = buildStatusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Completed Dialog
            if (completedApkFile != null) {
                val apk = completedApkFile!!
                AlertDialog(
                    onDismissRequest = {
                        completedApkFile = null
                        selectedAppToConfigure = null
                        currentTab = ScreenTab.HOME
                    },
                    containerColor = DarkSurface,
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(40.dp)) },
                    title = { Text("Cloned APK Ready!", fontWeight = FontWeight.Bold, color = TextPrimary) },
                    text = {
                        Column {
                            Text(
                                text = "Your customized APK has been generated:\n${apk.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Location: ${apk.parent}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                installApk(apk)
                                completedApkFile = null
                                selectedAppToConfigure = null
                                currentTab = ScreenTab.HOME
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text("Install Now")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                completedApkFile = null
                                selectedAppToConfigure = null
                                currentTab = ScreenTab.HOME
                            }
                        ) {
                            Text("Done", color = TextSecondary)
                        }
                    }
                )
            }

            return
        }

        // Standard Main Navigation
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = DarkSurface) {
                    ScreenTab.values().forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = DarkBackground
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> {
                        HomeScreen(
                            profileState = profileState,
                            clonedApps = clonedApps,
                            onSetupSandbox = {
                                val intent = profileManager.createProvisioningIntent()
                                provisioningLauncher.launch(intent)
                            },
                            onLaunchApp = { pkg ->
                                val success = profileManager.launchClonedApp(pkg)
                                if (!success) {
                                    Toast.makeText(this@MainActivity, "Could not launch $pkg in sandbox", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCloneNewClick = { currentTab = ScreenTab.PICKER },
                            onTogglePause = { paused ->
                                profileManager.setSandboxPaused(paused)
                            }
                        )
                    }
                    ScreenTab.PICKER -> {
                        AppPickerScreen(
                            installedApps = hostApps,
                            onConfigureApp = { app ->
                                selectedAppToConfigure = app
                            },
                            onBackClick = { currentTab = ScreenTab.HOME }
                        )
                    }
                    ScreenTab.PRESETS -> {
                        PresetsScreen()
                    }
                    ScreenTab.SETTINGS -> {
                        SettingsScreen(
                            onTriggerShizukuRequest = {
                                val ok = ShizukuBridge.requestShizukuPermission(1001)
                                if (!ok) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Shizuku service is not running. (Notice: Standalone cloning works without Shizuku!)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not launch installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
