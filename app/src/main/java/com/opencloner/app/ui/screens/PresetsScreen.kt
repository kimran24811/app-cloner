package com.opencloner.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencloner.app.data.model.CloningPreset
import com.opencloner.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    presets: List<CloningPreset> = CloningPreset.getDefaultPresets()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloning Presets", fontWeight = FontWeight.Bold) },
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
            item {
                Text(
                    text = "Zero-Config Intent Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Instead of 500+ confusing switches, OpenCloner automatically tunes low-level container policies based on what you need.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            items(presets) { preset ->
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
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndigoPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = preset.type.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = DarkSurfaceElevated)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Included Policies:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        preset.features.forEach { feat ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feat,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
