# OpenCloner

> **A free, open-source, ban-proof Android app cloner & sandbox.**  
> Built to replace predatory paywalls, "color packages", and broken signatures with native Android Enterprise multi-user sandboxing.

---

## 🌟 Why OpenCloner?

App Cloner and traditional cloning tools rely on decompiling, patching smali bytecode, and re-signing APKs. This causes major issues:
* **Account Bans:** Games (PUBG, Mobile Legends) and messaging apps (WhatsApp) detect signature changes and ban users.
* **Broken Notifications:** Firebase Cloud Messaging (FCM) tokens fail because package names don't match Google credentials.
* **Predatory Pricing:** Features locked behind "Blue/Orange packages", recurring $50/yr subscriptions, and arbitrary device blocks.
* **Deprecated Target SDK:** Older cloners target Android 9 (API 28), causing installation blocks and crashes on Android 14, 15, and 16.

### The OpenCloner Solution:
* ✅ **100% Free & Open-Source:** No subscriptions, no ads, no paywalled feature packs.
* ✅ **Native Sandboxing:** Uses Android's official `DevicePolicyManager` (Work Profiles). The target app's original cryptographic signature is **never modified**, making it **100% ban-proof**.
* ✅ **Working Push Notifications:** Google Play Services and FCM tokens work natively.
* ✅ **Modern Android Support:** Built with Jetpack Compose, Kotlin 2.0, targeting **API 34/35+**.
* ✅ **Intent-Driven Smart Presets:** Choose from 1-click presets (*Dual Social*, *Game Sanctuary*, *Burner/Privacy*, *Dev Suite*) instead of 500+ confusing checkboxes.
* ✅ **Shizuku Integration:** Enables elevated management and silent operations without needing root.

---

## 🚀 Intent-Driven Presets

| Preset | Target Use Case | Key Protections |
| :--- | :--- | :--- |
| **Dual Social** | WhatsApp, Telegram, Messenger | Full background sync, dual media storage, native notifications. |
| **Game Sanctuary** | Mobile Legends, PUBG, COD | Untouched signatures, zero hook detection, uncapped frame rate. |
| **Burner / Privacy** | Testing untrusted apps, browser sandboxes | Zero cross-app queries, isolated contacts, auto-freeze on lock. |
| **Dev Suite** | QA & Reverse Engineering | Direct logcat streaming, isolated storage inspection. |

---

## 🛠️ Project Structure

```
open-cloner/
├── .github/workflows/
│   └── build-apk.yml               # Free automated Cloud CI build pipeline
├── app/
│   ├── build.gradle.kts            # App module configuration (API 34, Compose)
│   └── src/main/
│       ├── AndroidManifest.xml     # Device Admin & Shizuku provider definitions
│       ├── java/com/opencloner/app/
│       │   ├── MainActivity.kt     # App entry, bottom navigation, provisioning flow
│       │   ├── OpenClonerApp.kt    # Base Application class
│       │   ├── data/model/         # Data contracts (AppItem, Presets, ProfileState)
│       │   ├── engine/             # WorkProfileManager, CloneAdminReceiver, ShizukuBridge
│       │   └── ui/                 # Material 3 Jetpack Compose Theme & Screens
│       └── res/                    # Adaptive launcher icons, strings, admin policies
├── gradle/
│   ├── libs.versions.toml          # Centralized dependency catalog
│   └── wrapper/                    # Gradle 8.7 Wrapper
└── build.gradle.kts                # Top-level Gradle configuration
```

---

## 📦 How to Build the APK

### Option 1: Automatic Cloud Build (Recommended for Non-Coders)
Push this repository to your personal GitHub account. The included `.github/workflows/build-apk.yml` file will automatically compile the project on GitHub's cloud servers and provide a direct download link for the `.apk` file under the **Actions** tab.

### Option 2: Local Build via Command Line
Run the following command from the root project directory:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot
.\gradlew.bat assembleDebug

# Linux / macOS
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug
```
The compiled APK will be output to:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License
This project is licensed under the **Apache License 2.0**.
