# NativeWebBridge 🌉

A robust, type-safe, bidirectional communication bridge between WebViews and Native iOS/Android apps.

## 🚀 Features
- **3-in-1 Monorepo:** Contains NPM Web Core, iOS Swift Package, and Android Gradle Module.
- **Asynchronous:** Promise-based architecture prevents UI blocking.
- **Safe Serialization:** Modern UTF-8 Base64 encoding prevents crashes from emojis and special characters.

## 📦 Installation

**Web (NPM):**
\`npm install native-web-bridge\`

**iOS (SPM):**
Add the GitHub repository URL in Xcode via **File > Add Packages**.

**Android (JitPack):**
Add \`maven { url 'https://jitpack.io' }\` to your root build.gradle, then:
\`implementation 'com.github.YourUsername:native-web-bridge:1.0.0'\`