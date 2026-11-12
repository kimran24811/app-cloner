package com.opencloner.app.data.model

import android.graphics.Bitmap

data class CloneConfig(
    val sourcePackageName: String,
    val sourceAppName: String,
    val sourceApkPath: String,
    var splitApkPaths: List<String> = emptyList(),

    // Identity Customization
    var customAppName: String = sourceAppName,
    var cloneNumber: Int = 1,
    var customPackageName: String = "$sourcePackageName.clone1",

    // Icon Customization
    var iconHueShift: Float = 0f, // 0 to 360 degrees
    var iconRotateDegrees: Float = 0f, // 0, 90, 180, 270
    var iconFlipHorizontal: Boolean = false,
    var badgeText: String = "",
    var chameleonDisguise: String = "", // Calculator, Clock, Weather, Notes
    var customIconBitmap: Bitmap? = null,

    // 🛡️ Privacy & Password Options
    var passwordProtect: Boolean = false,
    var appPassword: String = "",
    var fakeCalculatorLock: Boolean = false,
    var fakeCrashOnStartup: Boolean = false,
    var preventScreenshots: Boolean = false,
    var hideFromRecents: Boolean = false,
    var hideRoot: Boolean = false,
    var hideDeveloperMode: Boolean = false,

    // 🕵️ Identity & Anti-Fingerprinting Options
    var changeAndroidId: Boolean = false,
    var customAndroidId: String = "",
    var randomizeBuildProps: Boolean = false,
    var spoofDeviceModel: String = "",
    var mockWifiMac: Boolean = false,

    // 🌐 Networking & Proxy Options
    var enableProxy: Boolean = false,
    var proxyType: String = "SOCKS5", // SOCKS5 or HTTP
    var proxyHost: String = "",
    var proxyPort: Int = 1080,
    var proxyUser: String = "",
    var proxyPass: String = "",
    var offlineMode: Boolean = false, // Disable all network access

    // 📍 Location Mocking Options
    var mockLocation: Boolean = false,
    var mockLatitude: Double = 37.7749,
    var mockLongitude: Double = -122.4194,
    var hideMockLocation: Boolean = false,

    // 🖥️ Display & Media Options
    var forceDarkMode: Boolean = false,
    var forceOrientation: String = "", // PORTRAIT, LANDSCAPE
    var immersiveMode: Boolean = false,
    var muteAudioOnStart: Boolean = false,
    var keepScreenOn: Boolean = false,
    var fakeCamera: Boolean = false,

    // 📦 Storage & Sandboxing
    var isolateExternalStorage: Boolean = false,
    var autoClearCacheOnExit: Boolean = false,

    // 🚀 Launching & Automation
    var autoStartOnBoot: Boolean = false,
    var secretDialerCode: String = "",

    // 🔔 Notifications
    var blockNotifications: Boolean = false,
    var notificationKeywordFilter: String = "",

    // 🎮 Game & Performance
    var fpsLimiter: Int = 0,
    var touchVisualizer: Boolean = false,
    var requestLargeHeap: Boolean = false
) {
    fun updatePackageName() {
        customPackageName = "$sourcePackageName.clone$cloneNumber"
    }
}
