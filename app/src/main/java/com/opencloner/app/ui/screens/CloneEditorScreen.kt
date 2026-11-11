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
            sourceApkPath = sourceApkPath
        )
    }

    var appName by remember { mutableStateOf(config.customAppName) }
    var cloneNumber by remember { mutableStateOf(config.cloneNumber) }
    var hueShift by remember { mutableStateOf(config.iconHueShift) }
    var rotateDegrees by remember { mutableStateOf(config.iconRotateDegrees) }
    var flipHorizontal by remember { mutableStateOf(config.iconFlipHorizontal) }

    // Privacy
    var passwordProtect by remember { mutableStateOf(config.passwordProtect) }
    var appPassword by remember { mutableStateOf(config.appPassword) }
    var preventScreenshots by remember { mutableStateOf(config.preventScreenshots) }
    var hideRoot by remember { mutableStateOf(config.hideRoot) }

    // Identity
    var changeAndroidId by remember { mutableStateOf(config.changeAndroidId) }
    var customAndroidId by remember { mutableStateOf(config.customAndroidId) }
    var randomizeBuildProps by remember { mutableStateOf(config.randomizeBuildProps) }

    // Network
    var enableProxy by remember { mutableStateOf(config.enableProxy) }
    var proxyType by remember { mutableStateOf(config.proxyType) }
    var proxyHost by remember { mutableStateOf(config.proxyHost) }
    var proxyPort by remember { mutableStateOf(config.proxyPort.toString()) }
    var offlineMode by remember { mutableStateOf(config.offlineMode) }

    // Location
    var mockLocation by remember { mutableStateOf(config.mockLocation) }
    var mockLat by remember { mutableStateOf(config.mockLatitude.toString()) }
    var mockLng by remember { mutableStateOf(config.mockLongitude.toString()) }

    // Media & Display
    var forceDarkMode by remember { mutableStateOf(config.forceDarkMode) }
    var fakeCamera by remember { mutableStateOf(config.fakeCamera) }
    var muteAudio by remember { mutableStateOf(config.muteAudioOnStart) }
    var keepScreenOn by remember { mutableStateOf(config.keepScreenOn) }

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
                        onClick = {
                            config.customAppName = appName
                            config.cloneNumber = cloneNumber
                            config.updatePackageName()
                            config.iconHueShift = hueShift
                            config.iconRotateDegrees = rotateDegrees
                            config.iconFlipHorizontal = flipHorizontal
                            config.passwordProtect = passwordProtect
                            config.appPassword = appPassword
                            config.preventScreenshots = preventScreenshots
                            config.hideRoot = hideRoot
                            config.changeAndroidId = changeAndroidId
                            config.customAndroidId = customAndroidId
                            config.randomizeBuildProps = randomizeBuildProps
                            config.enableProxy = enableProxy
                            config.proxyType = proxyType
                            config.proxyHost = proxyHost
                            config.proxyPort = proxyPort.toIntOrNull() ?: 1080
                            config.offlineMode = offlineMode
                            config.mockLocation = mockLocation
                            config.mockLatitude = mockLat.toDoubleOrNull() ?: 37.7749
                            config.mockLongitude = mockLng.toDoubleOrNull() ?: -122.4194
                            config.forceDarkMode = forceDarkMode
                            config.fakeCamera = fakeCamera
                            config.muteAudioOnStart = muteAudio
                            config.keepScreenOn = keepScreenOn

                            onStartBuild(config)
                        },
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
            // Live Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live Transformed Icon Preview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated)
                                .rotate(rotateDegrees)
                                .scale(scaleX = if (flipHorizontal) -1f else 1f, scaleY = 1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = if (hueShift > 0f) Color.hsl(hueShift, 0.8f, 0.6f) else CyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName.ifBlank { app.appName },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${app.packageName}.clone$cloneNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                            Text(
                                text = "Standalone Modified APK",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            // 🎨 Name & Icon Customization Section
            item {
                FeatureGroupCard(title = "App Identity & Icon", icon = Icons.Default.Palette) {
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clone Number Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clone Number", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (cloneNumber > 1) cloneNumber-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = TextSecondary)
                            }
                            Text(
                                text = cloneNumber.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { cloneNumber++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = IndigoPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Icon Color Hue (${hueShift.toInt()}°)", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Slider(
                        value = hueShift,
                        onValueChange = { hueShift = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = IndigoPrimary,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = DarkSurfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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
                }
            }

            // 🛡️ Privacy & Password Protection
            item {
                FeatureGroupCard(title = "Privacy & Password Lock", icon = Icons.Default.Lock) {
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
                                label = { Text("Enter App Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customFieldColors()
                            )
                        }
                    }
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Prevent Screenshots",
                        subtitle = "Blocks screen capture and hides app preview in recents",
                        checked = preventScreenshots,
                        onCheckedChange = { preventScreenshots = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Hide Root & Debugging",
                        subtitle = "Conceals root binaries and test-keys from target app",
                        checked = hideRoot,
                        onCheckedChange = { hideRoot = it }
                    )
                }
            }

            // 🕵️ Identity & Anti-Fingerprinting
            item {
                FeatureGroupCard(title = "Device Identity & Spoofing", icon = Icons.Default.Fingerprint) {
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
                        title = "Randomize Build Props",
                        subtitle = "Spoofs device brand, model, and hardware fingerprint",
                        checked = randomizeBuildProps,
                        onCheckedChange = { randomizeBuildProps = it }
                    )
                }
            }

            // 🌐 Networking & SOCKS5 Proxy
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
                        subtitle = "Completely disable all internet permissions for this app",
                        checked = offlineMode,
                        onCheckedChange = { offlineMode = it }
                    )
                }
            }

            // 📍 Location Mocking (Fake GPS)
            item {
                FeatureGroupCard(title = "Location & Mock GPS", icon = Icons.Default.LocationOn) {
                    ToggleRow(
                        title = "Fake GPS Location",
                        subtitle = "Inject custom GPS coordinates directly into location provider",
                        checked = mockLocation,
                        onCheckedChange = { mockLocation = it }
                    )
                    AnimatedVisibility(visible = mockLocation) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
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
                    }
                }
            }

            // 🖥️ Display & Media
            item {
                FeatureGroupCard(title = "Display & Media Options", icon = Icons.Default.Tv) {
                    ToggleRow(
                        title = "Force Dark Mode",
                        subtitle = "Invert background and surfaces to dark colors",
                        checked = forceDarkMode,
                        onCheckedChange = { forceDarkMode = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Fake Camera",
                        subtitle = "Replace live camera feed with an image from your gallery",
                        checked = fakeCamera,
                        onCheckedChange = { fakeCamera = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Mute Volume on Start",
                        subtitle = "Silences all media playback when the cloned app opens",
                        checked = muteAudio,
                        onCheckedChange = { muteAudio = it }
                    )
                    Divider(color = DarkSurfaceElevated, modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Keep Screen On",
                        subtitle = "Prevents device display from sleeping while app is in foreground",
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it }
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(40.dp))
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
