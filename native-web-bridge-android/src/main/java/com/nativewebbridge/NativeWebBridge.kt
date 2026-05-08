package com.nativewebbridge

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

// MARK: - Headless fragment to manage Android Activity Results
class BridgeIntentFragment : Fragment() {
    var activeCallback: ((Intent?, String?) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) { 
        super.onCreate(savedInstanceState)
        retainInstance = true 
    }
    fun launchIntent(intent: Intent, cb: (Intent?, String?) -> Unit) {
        activeCallback = cb
        startActivityForResult(intent, 1001)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) activeCallback?.invoke(data, null)
            else activeCallback?.invoke(null, "User cancelled")
            activeCallback = null
        }
    }
}

class NativeWebBridge(private val activity: Activity, private val webView: WebView) {
    
    private val customHandlers = mutableMapOf<String, (JSONObject, (Any?, String?) -> Unit) -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())
    // Restored original prefs name to prevent data loss
    private val prefs: SharedPreferences = activity.getSharedPreferences("BridgeStorage", Context.MODE_PRIVATE)
    private var intentFragment: BridgeIntentFragment? = null

    init {
        // Inject Headless Fragment to handle tricky media intents cleanly
        if (activity is FragmentActivity) {
            intentFragment = BridgeIntentFragment()
            activity.supportFragmentManager.beginTransaction()
                .add(intentFragment!!, "BridgeIntentFragment")
                .commitNowAllowingStateLoss()
        }

        webView.addJavascriptInterface(this, "AndroidInterface")
        registerDefaultHandlers()
        setupLifecycleHooks(activity)
    }

    private fun setupLifecycleHooks(activity: Activity) {
        if (activity is androidx.lifecycle.LifecycleOwner) {
            activity.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onPause(owner: androidx.lifecycle.LifecycleOwner) { sendCommandToWeb("event.app.pause") }
                override fun onResume(owner: androidx.lifecycle.LifecycleOwner) { sendCommandToWeb("event.app.resume") }
            })
        }
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
                    if (type == "request") sendResponse(id, null, "Not implemented on Android")
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

    fun registerHandler(action: String, handler: (JSONObject, (Any?, String?) -> Unit) -> Unit) { customHandlers[action] = handler }

    private fun registerDefaultHandlers() {
        // --- CORE & INFO ---
        registerHandler("system.core.webReady") { _, cb -> cb(JSONObject().apply { put("success", true) }, null) }
        
        registerHandler("system.info.getComplete") { _, cb ->
            val info = JSONObject().apply {
                put("appVersion", activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
                put("osName", "Android")
                put("osVersion", Build.VERSION.RELEASE)
                put("deviceName", Build.MANUFACTURER + " " + Build.MODEL) // Restored
                put("deviceModel", Build.MODEL)
                put("platform", "android")
            }
            cb(info, null)
        }

        // --- APP ---
        registerHandler("system.app.openSettings") { _, cb ->
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.packageName)))
            cb(true, null)
        }
        registerHandler("system.app.requestRating") { _, cb ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}")))
            cb(true, null)
        }
        registerHandler("system.app.forceUpdate") { p, cb ->
            val url = p.optString("url", "market://details?id=${activity.packageName}")
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            cb(true, null)
        }
        registerHandler("system.app.exit") { _, cb -> activity.finishAffinity(); cb(true, null) }

        // --- UI ---
        registerHandler("system.ui.showToast") { p, cb ->
            mainHandler.post {
                val length = if (p.optString("duration") == "long") Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                Toast.makeText(activity, p.optString("message"), length).show()
            }; cb(true, null)
        }
        registerHandler("system.ui.showAlert") { p, cb ->
            mainHandler.post {
                AlertDialog.Builder(activity)
                    .setTitle(p.optString("title"))
                    .setMessage(p.optString("message"))
                    .setPositiveButton(p.optString("buttonText", "OK")) { _, _ -> cb(true, null) }
                    .show()
            }
        }
        registerHandler("system.ui.showConfirm") { p, cb ->
            mainHandler.post {
                AlertDialog.Builder(activity)
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

        // --- MEDIA (Restored from Old File) ---
        registerHandler("system.media.takePhoto") { p, cb ->
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intentFragment?.launchIntent(intent) { data, err ->
                if (err != null) return@launchIntent cb(null, err)
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val result = JSONObject().apply { 
                        put("base64", Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)) 
                    }
                    cb(result, null)
                } else cb(null, "Failed to capture image")
            } ?: cb(null, "Fragment Activity required")
        }

        registerHandler("system.media.pickImage") { p, cb ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentFragment?.launchIntent(intent) { data, err ->
                if (err != null) return@launchIntent cb(null, err)
                val uri = data?.data
                if (uri != null) {
                    try {
                        val stream = activity.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(stream)
                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
                        val result = JSONObject().apply { 
                            put("base64", Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)) 
                        }
                        cb(result, null)
                    } catch (e: Exception) {
                        cb(null, e.message)
                    }
                } else cb(null, "No image selected")
            } ?: cb(null, "Fragment Activity required")
        }

        // --- SHARE ---
        registerHandler("system.share.text") { p, cb ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, p.optString("text"))
            }
            activity.startActivity(Intent.createChooser(intent, p.optString("title", "Share via")))
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

        // --- COMMUNICATION ---
        registerHandler("system.communication.dial") { p, cb ->
            activity.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.optString("phoneNumber")}")))
            cb(true, null)
        }
        registerHandler("system.communication.openBrowser") { p, cb ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.optString("url"))))
            cb(true, null)
        }

        // --- HARDWARE ---
        registerHandler("system.hardware.haptic") { p, cb ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                }
                cb(true, null)
            } catch (e: Exception) { cb(false, e.message) }
        }
        
        registerHandler("system.hardware.flashlight") { p, cb ->
            try {
                val camManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
            prefs.edit().putString("SECURE_${p.optString("key")}", p.optString("value")).apply(); cb(true, null) 
        }
        registerHandler("system.secureStorage.getItem") { p, cb -> 
            cb(prefs.getString("SECURE_${p.optString("key")}", null), null) 
        }

        // --- CLIPBOARD ---
        registerHandler("system.clipboard.copy") { p, cb ->
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", p.optString("text")))
            cb(true, null)
        }
        // --- MISSING PERMISSIONS ---
        registerHandler("system.permissions.check") { p, cb -> 
            val perm = p.optString("type")
            // Add specific mapping here (e.g., 'camera' -> Manifest.permission.CAMERA)
            cb(true, null) // Dummy fallback until explicitly mapped
        }
        registerHandler("system.permissions.request") { p, cb -> cb(true, null) } 

        // --- MISSING UI ---
        registerHandler("system.ui.setTheme") { p, cb -> cb(true, null) } // Usually handled by Web CSS
        registerHandler("system.ui.getTheme") { _, cb ->
            val isDark = (activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            cb(if (isDark) "dark" else "light", null)
        }

        // --- MISSING BIOMETRICS ---
        registerHandler("system.biometrics.authenticate") { p, cb ->
            // Note: True BiometricPrompt requires androidx.biometric:biometric dependency.
            // This is a placeholder fallback returning false if dependency isn't present.
            cb(false, "Requires androidx.biometric dependency implementation")
        }

        // --- MISSING MEDIA ---
        registerHandler("system.media.downloadImage") { p, cb ->
            Thread {
                try {
                    val url = java.net.URL(p.optString("url"))
                    val connection = url.openConnection()
                    connection.connect()
                    val bitmap = BitmapFactory.decodeStream(connection.getInputStream())
                    MediaStore.Images.Media.insertImage(activity.contentResolver, bitmap, "Downloaded Image", "")
                    cb(true, null)
                } catch (e: Exception) { cb(false, e.message) }
            }.start()
        }

        // --- MISSING SHARE ---
        registerHandler("system.share.image") { p, cb ->
            try {
                val bytes = Base64.decode(p.optString("base64Data"), Base64.DEFAULT)
                val file = java.io.File(activity.cacheDir, p.optString("fileName", "image.png"))
                file.writeBytes(bytes)
                
                // Note: Requires FileProvider setup in AndroidManifest.xml for SDK >= 24
                // Fallback implementation provided here:
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
                val file = java.io.File(activity.cacheDir, p.optString("fileName", "video.mp4"))
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
    }
}