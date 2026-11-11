package com.opencloner.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.opencloner.app.data.model.AppItem
import com.opencloner.app.data.model.PresetType
import com.opencloner.app.engine.ShizukuBridge
import com.opencloner.app.engine.WorkProfileManager
import com.opencloner.app.ui.screens.*
import com.opencloner.app.ui.theme.DarkBackground
import com.opencloner.app.ui.theme.DarkSurface
import com.opencloner.app.ui.theme.IndigoPrimary
import com.opencloner.app.ui.theme.OpenClonerTheme
import com.opencloner.app.ui.theme.TextMuted
import com.opencloner.app.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenTab(val title: String, val icon: ImageVector) {
    HOME("Clones", Icons.Default.Layers),
    PICKER("Clone App", Icons.Default.AddCircle),
    PRESETS("Presets", Icons.Default.Tune),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var profileManager: WorkProfileManager

    private val provisioningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Sandbox Container configured successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Provisioning cancelled or completed in background.", Toast.LENGTH_SHORT).show()
        }
        profileManager.refreshState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileManager = WorkProfileManager.getInstance(this)

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
                            onCloneSelected = { app, preset ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val success = profileManager.cloneApp(app.packageName, preset)
                                    withContext(Dispatchers.Main) {
                                        if (success) {
                                            Toast.makeText(this@MainActivity, "Successfully cloned ${app.appName}!", Toast.LENGTH_SHORT).show()
                                            currentTab = ScreenTab.HOME
                                        } else {
                                            Toast.makeText(this@MainActivity, "Cloning requested for ${app.appName}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
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
                                ShizukuBridge.requestShizukuPermission(1001)
                            }
                        )
                    }
                }
            }
        }
    }
}
