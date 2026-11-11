package com.opencloner.app.data.model

enum class PresetType {
    SOCIAL,
    PRIVACY,
    GAMING,
    DEV
}

data class CloningPreset(
    val type: PresetType,
    val title: String,
    val tag: String,
    val description: String,
    val features: List<String>,
    val iconName: String
) {
    companion object {
        fun getDefaultPresets(): List<CloningPreset> = listOf(
            CloningPreset(
                type = PresetType.SOCIAL,
                title = "Dual Social",
                tag = "Recommended for WhatsApp / Telegram",
                description = "Keeps background push notifications active, isolates media storage, and operates seamlessly without battery throttling.",
                features = listOf(
                    "Native Google Play Services & FCM push tokens",
                    "Independent media & document storage",
                    "Dual background synchronization enabled",
                    "Zero account ban risk"
                ),
                iconName = "Chat"
            ),
            CloningPreset(
                type = PresetType.PRIVACY,
                title = "Burner / Privacy Sandbox",
                tag = "Zero cross-app tracking",
                description = "Complete sandbox isolation. Clones cannot query other installed apps, access main contacts, or read clipboard data.",
                features = listOf(
                    "Blocked cross-profile intent queries",
                    "Isolated contacts & call logs",
                    "No background activity when closed",
                    "Auto-freeze container on lock"
                ),
                iconName = "Shield"
            ),
            CloningPreset(
                type = PresetType.GAMING,
                title = "Game Sanctuary",
                tag = "100% Anti-Cheat Ban Proof",
                description = "Safe for competitive games (PUBG, Mobile Legends, COD). Preserves original cryptographic signature without memory hooking.",
                features = listOf(
                    "Original APK signature intact",
                    "No Xposed/Frida/Smali hook detection",
                    "Independent save states & guest accounts",
                    "Uncapped display frame rate"
                ),
                iconName = "SportsEsports"
            ),
            CloningPreset(
                type = PresetType.DEV,
                title = "Developer & Test Suite",
                tag = "Inspection & Diagnostics",
                description = "Designed for QA and reverse engineering. Direct logcat stream access and isolated SQLite inspection.",
                features = listOf(
                    "Shizuku elevated binder access",
                    "Inspectable internal data directory",
                    "Custom intent broadcast testing",
                    "Logcat filtering per sandbox profile"
                ),
                iconName = "Terminal"
            )
        )
    }
}
