package com.opencloner.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencloner.app.data.model.AppItem
import com.opencloner.app.data.model.ProfileState
import com.opencloner.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profileState: ProfileState,
    clonedApps: List<AppItem>,
    onSetupSandbox: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onCloneNewClick: () -> Unit,
    onTogglePause: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (profileState is ProfileState.Active) EmeraldSuccess else AmberWarning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OpenCloner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (profileState is ProfileState.Active) {
                        IconButton(onClick = { onTogglePause(!profileState.isPaused) }) {
                            Icon(
                                imageVector = if (profileState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Toggle Sandbox Freeze",
                                tint = if (profileState.isPaused) AmberWarning else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCloneNewClick,
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Clone App", fontWeight = FontWeight.SemiBold) }
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
            // Status Card
            item {
                when (profileState) {
                    is ProfileState.Unprovisioned -> {
                        UnprovisionedBanner(onSetup = onSetupSandbox)
                    }
                    is ProfileState.Active -> {
                        ActiveProfileCard(
                            profileState = profileState,
                            clonedCount = clonedApps.size,
                            onTogglePause = onTogglePause
                        )
                    }
                    else -> {}
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Clones (${clonedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "100% Ban-Proof",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                }
            }

            if (clonedApps.isEmpty()) {
                item {
                    EmptyClonesView(onCloneClick = onCloneNewClick)
                }
            } else {
                items(clonedApps) { app ->
                    CloneItemCard(app = app, onLaunch = { onLaunchApp(app.packageName) })
                }
            }
        }
    }
}

@Composable
fun UnprovisionedBanner(onSetup: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sandbox Not Configured",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OpenCloner utilizes Android's native isolated Work Profile container. It gives you 100% ban-proof dual apps with zero root or ads.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSetup,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Activate Sandbox (1-Click Setup)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveProfileCard(
    profileState: ProfileState.Active,
    clonedCount: Int,
    onTogglePause: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoDark.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sandbox Container Active",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (profileState.isPaused) "Status: Frozen (Sleeping)" else "Status: Running & Syncing",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (profileState.isPaused) AmberWarning else EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DarkSurfaceElevated)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Cloned Apps", value = clonedCount.toString())
                StatColumn(label = "Isolation", value = "Hardware")
                StatColumn(label = "Push Service", value = "Direct FCM")
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun CloneItemCard(app: AppItem, onLaunch: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            IconButton(
                onClick = onLaunch,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = IndigoPrimary)
            ) {
                Icon(Icons.Default.Launch, contentDescription = "Launch", tint = Color.White)
            }
        }
    }
}

@Composable
fun EmptyClonesView(onCloneClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LayersClear,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No apps cloned yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the button below to choose an app to duplicate.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
