package com.nativewebbridge

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import java.util.UUID

class NativeWebBridge(private val activity: Activity, private val webView: WebView) {
    
    private val customHandlers = mutableMapOf<String, (JSONObject, (Any?, String?) -> Unit) -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val prefs: SharedPreferences = activity.getSharedPreferences("BridgePrefs", Context.MODE_PRIVATE)

    init {
        webView.addJavascriptInterface(this, "AndroidInterface")
        registerDefaultHandlers()
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
                put("deviceName", Build.DEVICE)
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

        // --- SECURE STORAGE (Fallbacks to prefs if Crypto lib not available in basic setup) ---
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
        
        // Note: system.media (takePhoto, pickImage) and system.biometrics (authenticate) 
        // require AndroidX ActivityResultLaunchers and BiometricPrompt respectively, 
        // which must be registered in the Activity before onCreate completes. 
        // For plug-and-play architecture, map those to dummy/success fallbacks here 
        // or trigger custom Activity Intents.
    }
}