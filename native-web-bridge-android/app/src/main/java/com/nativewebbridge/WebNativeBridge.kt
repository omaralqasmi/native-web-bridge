package com.nativewebbridge

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject
import java.util.UUID

class WebNativeBridge(private val activity: FragmentActivity, private val webView: WebView) {

    private val customHandlers = mutableMapOf<String, (JSONObject, (Any?, String?) -> Unit) -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val prefs: SharedPreferences = activity.getSharedPreferences("WebNativeBridgeStorage", Context.MODE_PRIVATE)
    private var intentFragment: BridgeIntentFragment

    init {
        // 1. Inject Headless Fragment for Intents
        intentFragment = BridgeIntentFragment()
        activity.supportFragmentManager.beginTransaction()
            .add(intentFragment, "BridgeIntentFragment")
            .commitNowAllowingStateLoss()

        // 2. Attach to WebView
        webView.addJavascriptInterface(this, "AndroidInterface")

        // 3. Setup Lifecycle Events
        setupLifecycleHooks()

        // 4. Load all Handlers
        registerDefaultHandlers()
    }

    private fun setupLifecycleHooks() {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) { sendCommandToWeb("event.app.pause") }
            override fun onResume(owner: LifecycleOwner) { sendCommandToWeb("event.app.resume") }
        })
    }

    // Call this from your MainActivity's onBackPressedDispatcher
    fun triggerBackButton() {
        sendCommandToWeb("event.app.backButton")
    }

    @JavascriptInterface
    fun postMessage(b64String: String) {
        try {
            val jsonString = String(Base64.decode(b64String, Base64.DEFAULT))
            val json = JSONObject(jsonString)

            val type = json.optString("type")
            val id = json.optString("id")
            val action = json.optString("action")
            val payload = json.optJSONObject("payload") ?: JSONObject()

            if (type == "request" || type == "command") {
                val handler = customHandlers[action]
                if (handler != null) {
                    handler(payload) { resultPayload, errorMsg ->
                        if (type == "request") sendResponse(id, resultPayload, errorMsg)
                    }
                } else {
                    if (type == "request") sendResponse(id, null, "Action not implemented on Android: $action")
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun sendResponse(id: String, payload: Any?, error: String?) {
        val response = JSONObject().apply {
            put("id", id); put("type", "response")
            payload?.let { put("payload", it) }; error?.let { put("error", it) }
        }
        val base64 = Base64.encodeToString(response.toString().toByteArray(), Base64.NO_WRAP)
        mainHandler.post { webView.evaluateJavascript("window.NativeBridgeReceiver.receiveMessage('$base64')", null) }
    }

    fun sendCommandToWeb(action: String, payload: JSONObject? = null) {
        val request = JSONObject().apply {
            put("id", UUID.randomUUID().toString()); put("type", "command"); put("action", action)
            payload?.let { put("payload", it) }
        }
        val base64 = Base64.encodeToString(request.toString().toByteArray(), Base64.NO_WRAP)
        mainHandler.post { webView.evaluateJavascript("window.NativeBridgeReceiver.receiveMessage('$base64')", null) }
    }

    fun registerHandler(action: String, handler: (JSONObject, (Any?, String?) -> Unit) -> Unit) {
        customHandlers[action] = handler
    }
    private fun registerDefaultHandlers() {
        // --- CORE & INFO ---
        registerHandler("system.core.webReady") { _, cb -> cb(JSONObject().apply { put("success", true) }, null) }

        registerHandler("system.info.getComplete") { _, cb ->
            val info = JSONObject().apply {
                put("appVersion", activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
                put("osName", "Android")
                put("osVersion", android.os.Build.VERSION.RELEASE)
                put("deviceName", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL)
                put("deviceModel", android.os.Build.MODEL)
                put("platform", "android")
            }
            cb(info, null)
        }

        // --- PERMISSIONS ---
        registerHandler("system.permissions.check") { p, cb ->
            val type = p.optString("type")
            val manifestPerm = when(type) {
                "camera" -> android.Manifest.permission.CAMERA
                "location" -> android.Manifest.permission.ACCESS_FINE_LOCATION
                else -> null
            }
            if (manifestPerm != null) {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(activity, manifestPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                cb(granted, null)
            } else cb(true, null)
        }
        registerHandler("system.permissions.request") { p, cb -> cb(true, null) } // Can be expanded with ActivityResultContracts later

        // --- APP ---
        registerHandler("system.app.openSettings") { _, cb ->
            activity.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + activity.packageName)))
            cb(true, null)
        }
        registerHandler("system.app.requestRating") { _, cb ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${activity.packageName}")))
            cb(true, null)
        }
        registerHandler("system.app.forceUpdate") { p, cb ->
            val url = p.optString("url", "market://details?id=${activity.packageName}")
            activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            cb(true, null)
        }
        registerHandler("system.app.exit") { _, cb -> activity.finishAffinity(); cb(true, null) }

        // --- UI ---
        registerHandler("system.ui.setTheme") { _, cb -> cb(true, null) } // Usually handled by CSS, hard to force natively on the fly without restart
        registerHandler("system.ui.getTheme") { _, cb ->
            val isDark = (activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            cb(if (isDark) "dark" else "light", null)
        }
        registerHandler("system.ui.showToast") { p, cb ->
            mainHandler.post {
                val length = if (p.optString("duration") == "long") android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
                android.widget.Toast.makeText(activity, p.optString("message"), length).show()
            }
            cb(true, null)
        }
        registerHandler("system.ui.showAlert") { p, cb ->
            mainHandler.post {
                androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle(p.optString("title"))
                    .setMessage(p.optString("message"))
                    .setPositiveButton(p.optString("buttonText", "OK")) { _, _ -> cb(true, null) }
                    .show()
            }
        }
        registerHandler("system.ui.showConfirm") { p, cb ->
            mainHandler.post {
                androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle(p.optString("title"))
                    .setMessage(p.optString("message"))
                    .setPositiveButton(p.optString("okText", "Yes")) { _, _ -> cb(true, null) }
                    .setNegativeButton(p.optString("cancelText", "No")) { _, _ -> cb(false, null) }
                    .setCancelable(false)
                    .show()
            }
        }

        // --- NOTIFICATIONS ---
        registerHandler("system.notifications.getToken") { _, cb -> cb(prefs.getString("PushToken", null), null) }
        registerHandler("system.notifications.setToken") { p, cb ->
            prefs.edit().putString("PushToken", p.optString("token")).apply(); cb(true, null)
        }

        // --- MEDIA ---
        registerHandler("system.media.takePhoto") { p, cb ->
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            intentFragment.launchIntent(intent) { data, err ->
                if (err != null) return@launchIntent cb(null, err)
                val bitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                if (bitmap != null) {
                    val stream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                    cb(JSONObject().apply { put("base64", Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)) }, null)
                } else cb(null, "Failed to capture image")
            }
        }
        registerHandler("system.media.pickImage") { p, cb ->
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentFragment.launchIntent(intent) { data, err ->
                if (err != null) return@launchIntent cb(null, err)
                val uri = data?.data
                if (uri != null) {
                    try {
                        val stream = activity.contentResolver.openInputStream(uri)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                        val out = java.io.ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, out)
                        cb(JSONObject().apply { put("base64", Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)) }, null)
                    } catch (e: Exception) { cb(null, e.message) }
                } else cb(null, "No image selected")
            }
        }
        registerHandler("system.media.downloadImage") { p, cb ->
            Thread {
                try {
                    val connection = java.net.URL(p.optString("url")).openConnection()
                    connection.connect()
                    val bitmap = android.graphics.BitmapFactory.decodeStream(connection.getInputStream())
                    android.provider.MediaStore.Images.Media.insertImage(activity.contentResolver, bitmap, "Downloaded Image", "")
                    cb(true, null)
                } catch (e: Exception) { cb(false, e.message) }
            }.start()
        }

        // --- SHARE ---
        registerHandler("system.share.text") { p, cb ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, p.optString("text"))
            }
            activity.startActivity(Intent.createChooser(intent, p.optString("title", "Share")))
            cb(true, null)
        }
        registerHandler("system.share.link") { p, cb ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, p.optString("url"))
            }
            activity.startActivity(Intent.createChooser(intent, p.optString("title", "Share Link")))
            cb(true, null)
        }
        registerHandler("system.share.image") { p, cb ->
            try {
                val bytes = Base64.decode(p.optString("base64Data"), Base64.DEFAULT)
                val file = java.io.File(activity.cacheDir, p.optString("fileName", "shared_image.png"))
                file.writeBytes(bytes)
                val uri = androidx.core.content.FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, "Share Image"))
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }
        registerHandler("system.share.video") { p, cb ->
            try {
                val bytes = Base64.decode(p.optString("base64Data"), Base64.DEFAULT)
                val file = java.io.File(activity.cacheDir, p.optString("fileName", "shared_video.mp4"))
                file.writeBytes(bytes)
                val uri = androidx.core.content.FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, "Share Video"))
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }

        // --- COMMUNICATION ---
        registerHandler("system.communication.dial") { p, cb ->
            activity.startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:${p.optString("phoneNumber")}")))
            cb(true, null)
        }
        registerHandler("system.communication.openBrowser") { p, cb ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(p.optString("url"))))
            cb(true, null)
        }

        // --- HARDWARE ---
        registerHandler("system.hardware.haptic") { p, cb ->
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                    vibratorManager.defaultVibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                    vibrator.vibrate(50)
                }
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }
        registerHandler("system.hardware.flashlight") { p, cb ->
            try {
                val camManager = activity.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                val cameraId = camManager.cameraIdList[0]
                camManager.setTorchMode(cameraId, p.optBoolean("on", false))
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }

        // --- STORAGE ---
        registerHandler("system.storage.setItem") { p, cb ->
            val key = p.optString("key")
            when (val value = p.get("value")) {
                is String -> prefs.edit().putString(key, value).apply()
                is Boolean -> prefs.edit().putBoolean(key, value).apply()
                is Int -> prefs.edit().putInt(key, value).apply()
                else -> prefs.edit().putString(key, value.toString()).apply()
            }
            cb(true, null)
        }
        registerHandler("system.storage.getItem") { p, cb -> cb(prefs.all[p.optString("key")], null) }

        // --- SECURE STORAGE ---
        registerHandler("system.secureStorage.setItem") { p, cb ->
            try {
                val masterKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC)
                val securePrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                    "SecureWebNativeBridge", masterKeyAlias, activity,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                securePrefs.edit().putString(p.optString("key"), p.optString("value")).apply()
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }
        registerHandler("system.secureStorage.getItem") { p, cb ->
            try {
                val masterKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC)
                val securePrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                    "SecureWebNativeBridge", masterKeyAlias, activity,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                cb(securePrefs.getString(p.optString("key"), null), null)
            } catch (e: Exception) { cb(false, e.message) }
        }

        // --- BIOMETRICS ---
        registerHandler("system.biometrics.authenticate") { p, cb ->
            mainHandler.post {
                val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication Required")
                    .setSubtitle(p.optString("reason", "Please authenticate to continue"))
                    .setNegativeButtonText("Cancel")
                    .build()

                val biometricPrompt = androidx.biometric.BiometricPrompt(activity, executor,
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            cb(false, errString.toString())
                        }
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            cb(true, null)
                        }
                        override fun onAuthenticationFailed() {
                            cb(false, "Authentication failed")
                        }
                    })
                biometricPrompt.authenticate(promptInfo)
            }
        }

        // --- CLIPBOARD ---
        registerHandler("system.clipboard.copy") { p, cb ->
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Copied Text", p.optString("text")))
            cb(true, null)
        }
    }
}