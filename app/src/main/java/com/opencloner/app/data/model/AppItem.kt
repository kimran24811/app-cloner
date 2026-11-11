package com.opencloner.app.data.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isCloned: Boolean = false,
    val activePreset: PresetType = PresetType.SOCIAL
)
