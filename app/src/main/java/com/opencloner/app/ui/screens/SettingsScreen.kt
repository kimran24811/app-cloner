package com.opencloner.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencloner.app.engine.ShizukuBridge
import com.opencloner.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTriggerShizukuRequest: () -> Unit
) {
    var isShizukuAvailable by remember { mutableStateOf(ShizukuBridge.isShizukuAvailable()) }
    var hasShizukuPerm by remember { mutableStateOf(ShizukuBridge.hasShizukuPermission()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Environment", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elevated Permissions (Shizuku) Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Shizuku Privilege Bridge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Shizuku allows silent app installations and fine-grained sandbox control via ADB without needing root.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (hasShizukuPerm) "Connected & Authorized" else "Not connected",
                                color = if (hasShizukuPerm) EmeraldSuccess else AmberWarning,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (!hasShizukuPerm) {
                                Button(
                                    onClick = onTriggerShizukuRequest,
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant Access")
                                }
                            }
                        }
                    }
                }
            }

            // Trust & License Freedom Pledge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = RoseError)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Community Freedom Pledge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 100% Free & Open Source\n• No monthly/annual subscriptions\n• No 'Blue/Orange' paywalled add-on packages\n• No DRM device limits or 'User Blocked' bans\n• Fully on-device: zero telemetry tracking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                    }
                }
            }

            // Version info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OpenCloner v1.0.0 (Build 20261111)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Engineered with native Android Enterprise Sandboxing",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
