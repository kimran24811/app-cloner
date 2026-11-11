package com.opencloner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.opencloner.app.data.model.PresetType
import com.opencloner.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    installedApps: List<AppItem>,
    onCloneSelected: (AppItem, PresetType) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForClone by remember { mutableStateOf<AppItem?>(null) }
    var selectedPreset by remember { mutableStateOf(PresetType.SOCIAL) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select App to Clone", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps (e.g. WhatsApp, Instagram)...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = DarkSurfaceElevated,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAppForClone = app },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = CyanAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                            if (app.isCloned) {
                                Text(
                                    text = "Cloned",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Button(
                                    onClick = { selectedAppForClone = app },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Clone", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clone Configuration Dialog
        if (selectedAppForClone != null) {
            val app = selectedAppForClone!!
            AlertDialog(
                onDismissRequest = { selectedAppForClone = null },
                containerColor = DarkSurface,
                title = {
                    Text("Clone ${app.appName}", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Choose an Intent Preset for this clone:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        PresetOptionItem(
                            title = "Dual Social",
                            subtitle = "Full background sync & push notifications",
                            isSelected = selectedPreset == PresetType.SOCIAL,
                            onClick = { selectedPreset = PresetType.SOCIAL }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PresetOptionItem(
                            title = "Game Sanctuary",
                            subtitle = "100% Anti-Cheat ban proof, untouched signature",
                            isSelected = selectedPreset == PresetType.GAMING,
                            onClick = { selectedPreset = PresetType.GAMING }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PresetOptionItem(
                            title = "Burner / Privacy",
                            subtitle = "Zero cross-app queries, isolated files",
                            isSelected = selectedPreset == PresetType.PRIVACY,
                            onClick = { selectedPreset = PresetType.PRIVACY }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = selectedAppForClone
                            selectedAppForClone = null
                            if (target != null) {
                                onCloneSelected(target, selectedPreset)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Create Clone Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppForClone = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun PresetOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) IndigoPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IndigoPrimary)) else null
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}
