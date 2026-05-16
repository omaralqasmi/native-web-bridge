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
    private var isWebReady = false
    private val nativeMessageQueue = mutableListOf<String>()
    private var lastKeyboardState = false

    init {
        intentFragment = BridgeIntentFragment()
        activity.supportFragmentManager.beginTransaction().add(intentFragment, "BridgeIntentFragment").commitNowAllowingStateLoss()
        webView.addJavascriptInterface(this, "AndroidInterface")
        setupLifecycleHooks()
        setupKeyboardListener()
        setupNetworkListener() 
        setupBackButtonListener() // 👈 Auto-intercepts physical back button!
        registerDefaultHandlers()
    }

    private fun setupLifecycleHooks() {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) { sendCommandToWeb("event.app.pause") }
            override fun onResume(owner: LifecycleOwner) { sendCommandToWeb("event.app.resume") }
        })
    }

    private fun setupKeyboardListener() {
        val rootView = activity.findViewById<android.view.View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val isVisible = keypadHeight > screenHeight * 0.15
            if (isVisible != lastKeyboardState) {
                lastKeyboardState = isVisible
                sendCommandToWeb("event.ui.keyboardChanged", JSONObject().apply { put("isVisible", isVisible) })
            }
        }
    }

    private fun setupNetworkListener() {
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        cm.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) { sendCommandToWeb("event.network.statusChanged", JSONObject().apply { put("connected", true); put("type", "unknown") }) }
            override fun onLost(network: android.net.Network) { sendCommandToWeb("event.network.statusChanged", JSONObject().apply { put("connected", false); put("type", "none") }) }
        })
    }

    private fun setupBackButtonListener() {
        activity.onBackPressedDispatcher.addCallback(activity, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                sendCommandToWeb("event.app.backButton")
            }
        })
    }

    fun triggerBackButton() { sendCommandToWeb("event.app.backButton") }
    fun triggerDeepLink(url: String) { sendCommandToWeb("event.app.deepLink", JSONObject().apply { put("url", url) }) }

    @JavascriptInterface
    fun postMessage(b64String: String) {
        try {
            val json = JSONObject(String(Base64.decode(b64String, Base64.DEFAULT)))
            val type = json.optString("type")
            val id = json.optString("id")
            val action = json.optString("action")
            val payload = json.optJSONObject("payload") ?: JSONObject()

            if (type == "request" || type == "command") {
                val handler = customHandlers[action]
                if (handler != null) { handler(payload) { res, err -> if (type == "request") sendResponse(id, res, err) } } 
                else if (type == "request") sendResponse(id, null, "Action not implemented: $action")
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun dispatchToWeb(base64Payload: String) {
        val script = "if(window.NativeBridgeReceiver) { window.NativeBridgeReceiver.receiveMessage('$base64Payload'); }"
        mainHandler.post { if (isWebReady) webView.evaluateJavascript(script, null) else nativeMessageQueue.add(script) }
    }

    private fun flushNativeQueue() {
        mainHandler.post { nativeMessageQueue.forEach { webView.evaluateJavascript(it, null) }; nativeMessageQueue.clear() }
    }

    private fun sendResponse(id: String, payload: Any?, error: String?) {
        val response = JSONObject().apply { put("id", id); put("type", "response"); payload?.let { put("payload", it) }; error?.let { put("error", it) } }
        dispatchToWeb(Base64.encodeToString(response.toString().toByteArray(), Base64.NO_WRAP))
    }

    fun sendCommandToWeb(action: String, payload: JSONObject? = null) {
        val request = JSONObject().apply { put("id", UUID.randomUUID().toString()); put("type", "command"); put("action", action); payload?.let { put("payload", it) } }
        dispatchToWeb(Base64.encodeToString(request.toString().toByteArray(), Base64.NO_WRAP))
    }

    fun registerHandler(action: String, handler: (JSONObject, (Any?, String?) -> Unit) -> Unit) { customHandlers[action] = handler }

    private fun registerDefaultHandlers() {
        registerHandler("system.core.webReady") { _, cb -> isWebReady = true; flushNativeQueue(); cb(JSONObject().apply { put("success", true) }, null) }
        registerHandler("system.info.getComplete") { _, cb -> cb(JSONObject().apply { put("appVersion", activity.packageManager.getPackageInfo(activity.packageName, 0).versionName); put("osName", "Android"); put("osVersion", android.os.Build.VERSION.RELEASE); put("deviceName", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL); put("deviceModel", android.os.Build.MODEL); put("platform", "android") }, null) }
        registerHandler("system.device.getLanguage") { _, cb -> cb(java.util.Locale.getDefault().toLanguageTag(), null) }
        registerHandler("system.device.getBatteryLevel") { _, cb -> cb((activity.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager).getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY), null) }
        
        registerHandler("system.screen.setKeepScreenOn") { p, cb -> mainHandler.post { if (p.optBoolean("keepOn", true)) activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); cb(true, null) } }
        registerHandler("system.network.getStatus") { _, cb ->
            val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            val type = if (caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true) "wifi" else if (caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true) "cellular" else "none"
            cb(JSONObject().apply { put("connected", caps != null); put("type", type) }, null)
        }

        fun getManifestPerm(type: String): String? = when(type) { 
            "camera" -> android.Manifest.permission.CAMERA
            "location" -> android.Manifest.permission.ACCESS_FINE_LOCATION
            "contacts" -> android.Manifest.permission.READ_CONTACTS
            "notifications" -> if (android.os.Build.VERSION.SDK_INT >= 33) "android.permission.POST_NOTIFICATIONS" else null
            else -> null 
        }
        
        registerHandler("system.permissions.check") { p, cb -> val perm = getManifestPerm(p.optString("type")); if (perm != null) cb(androidx.core.content.ContextCompat.checkSelfPermission(activity, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED, null) else cb(true, null) }
        registerHandler("system.permissions.request") { p, cb -> val perm = getManifestPerm(p.optString("type")); if (perm != null) mainHandler.post { intentFragment.requestPermission(perm) { cb(it, null) } } else cb(true, null) }

        registerHandler("system.contacts.pick") { _, cb -> mainHandler.post { intentFragment.launchIntent(Intent(Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI)) { data, err -> if (err != null) cb(null, err) else if (data?.data != null) { val c = activity.contentResolver.query(data.data!!, null, null, null, null); if (c != null && c.moveToFirst()) { cb(JSONObject().apply { put("name", c.getString(c.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))); put("phoneNumber", c.getString(c.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))) }, null); c.close() } else cb(null, "Error") } } } }
        registerHandler("system.file.pick") { _, cb -> mainHandler.post { intentFragment.launchIntent(Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }) { data, err -> if (err != null) cb(null, err) else if (data?.data != null) { try { val s = activity.contentResolver.openInputStream(data.data!!); val b = s?.readBytes(); s?.close(); cb(JSONObject().apply { put("name", "file"); put("base64", Base64.encodeToString(b, Base64.NO_WRAP)) }, null) } catch (e: Exception) { cb(null, e.message) } } } } }
        registerHandler("system.location.getCurrent") { _, cb -> try { val lm = activity.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager; val loc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER); if (loc != null) cb(JSONObject().apply { put("lat", loc.latitude); put("lng", loc.longitude) }, null) else cb(null, "Unavailable") } catch (e: SecurityException) { cb(null, "Permission missing") } }
        registerHandler("system.audio.playSound") { _, cb -> try { android.media.RingtoneManager.getRingtone(activity, android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)).play(); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }
        
        registerHandler("system.app.openSettings") { _, cb -> activity.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + activity.packageName))); cb(true, null) }
        registerHandler("system.app.requestRating") { _, cb -> activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${activity.packageName}"))); cb(true, null) }
        registerHandler("system.app.forceUpdate") { p, cb -> activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(p.optString("url", "market://details?id=${activity.packageName}")))); cb(true, null) }
        registerHandler("system.app.exit") { _, cb -> activity.finishAffinity(); cb(true, null) }

        registerHandler("system.ui.setTheme") { p, cb -> mainHandler.post { val t = p.optString("theme", "system"); androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(if (t == "dark") androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES else if (t == "light") androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); cb(true, null) } }
        registerHandler("system.ui.getTheme") { _, cb -> cb(if ((activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES) "dark" else "light", null) }
        registerHandler("system.ui.showToast") { p, cb -> mainHandler.post { android.widget.Toast.makeText(activity, p.optString("message"), if (p.optString("duration") == "long") android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT).show(); cb(true, null) } }
        registerHandler("system.ui.showAlert") { p, cb -> mainHandler.post { androidx.appcompat.app.AlertDialog.Builder(activity).setTitle(p.optString("title")).setMessage(p.optString("message")).setPositiveButton(p.optString("buttonText", "OK")) { _, _ -> cb(true, null) }.show() } }
        registerHandler("system.ui.showConfirm") { p, cb -> mainHandler.post { androidx.appcompat.app.AlertDialog.Builder(activity).setTitle(p.optString("title")).setMessage(p.optString("message")).setPositiveButton(p.optString("okText", "Yes")) { _, _ -> cb(true, null) }.setNegativeButton(p.optString("cancelText", "No")) { _, _ -> cb(false, null) }.setCancelable(false).show() } }

        registerHandler("system.notifications.getToken") { _, cb -> cb(prefs.getString("PushToken", null), null) }
        registerHandler("system.notifications.setToken") { p, cb -> prefs.edit().putString("PushToken", p.optString("token")).apply(); cb(true, null) }

        registerHandler("system.media.takePhoto") { p, cb -> mainHandler.post { intentFragment.launchIntent(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)) { data, _ -> val b = data?.extras?.get("data") as? android.graphics.Bitmap; if (b != null) { val s = java.io.ByteArrayOutputStream(); b.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, s); cb(JSONObject().apply { put("base64", Base64.encodeToString(s.toByteArray(), Base64.NO_WRAP)) }, null) } else cb(null, "Error") } } }
        registerHandler("system.media.pickImage") { p, cb -> mainHandler.post { intentFragment.launchIntent(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)) { data, _ -> try { val s = activity.contentResolver.openInputStream(data?.data!!); val b = android.graphics.BitmapFactory.decodeStream(s); val o = java.io.ByteArrayOutputStream(); b.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, o); cb(JSONObject().apply { put("base64", Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP)) }, null) } catch (e: Exception) { cb(null, e.message) } } } }
        registerHandler("system.media.downloadImage") { p, cb -> Thread { try { val c = java.net.URL(p.optString("url")).openConnection(); c.connect(); val b = android.graphics.BitmapFactory.decodeStream(c.getInputStream()); android.provider.MediaStore.Images.Media.insertImage(activity.contentResolver, b, "Downloaded", ""); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }.start() }

        registerHandler("system.share.text") { p, cb -> activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, p.optString("text")) }, p.optString("title", "Share"))); cb(true, null) }
        registerHandler("system.share.link") { p, cb -> activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, p.optString("url")) }, p.optString("title", "Share"))); cb(true, null) }
        registerHandler("system.share.image") { p, cb -> try { val file = java.io.File(activity.cacheDir, p.optString("fileName", "shared.png")); file.writeBytes(Base64.decode(p.optString("base64Data"), Base64.DEFAULT)); val uri = androidx.core.content.FileProvider.getUriForFile(activity, "${activity.packageName}.webnativebridge.fileprovider", file); activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "image/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share Image")); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }
        registerHandler("system.share.video") { p, cb -> try { val file = java.io.File(activity.cacheDir, p.optString("fileName", "shared.mp4")); file.writeBytes(Base64.decode(p.optString("base64Data"), Base64.DEFAULT)); val uri = androidx.core.content.FileProvider.getUriForFile(activity, "${activity.packageName}.webnativebridge.fileprovider", file); activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "video/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share Video")); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }

        registerHandler("system.communication.dial") { p, cb -> activity.startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:${p.optString("phoneNumber")}"))); cb(true, null) }
        registerHandler("system.communication.openBrowser") { p, cb -> activity.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(p.optString("url")))); cb(true, null) }

        registerHandler("system.hardware.haptic") { p, cb -> try { val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator; v.vibrate(50); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }
        registerHandler("system.hardware.flashlight") { p, cb -> try { val c = activity.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager; c.setTorchMode(c.cameraIdList[0], p.optBoolean("on", false)); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }

        registerHandler("system.storage.setItem") { p, cb -> prefs.edit().putString(p.optString("key"), p.optString("value")).apply(); cb(true, null) }
        registerHandler("system.storage.getItem") { p, cb -> cb(prefs.getString(p.optString("key"), null), null) }
        registerHandler("system.secureStorage.setItem") { p, cb -> try { val s = androidx.security.crypto.EncryptedSharedPreferences.create("SecPrefs", androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC), activity, androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM); s.edit().putString(p.optString("key"), p.optString("value")).apply(); cb(true, null) } catch (e: Exception) { cb(false, e.message) } }
        registerHandler("system.secureStorage.getItem") { p, cb -> try { val s = androidx.security.crypto.EncryptedSharedPreferences.create("SecPrefs", androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC), activity, androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM); cb(s.getString(p.optString("key"), null), null) } catch (e: Exception) { cb(false, e.message) } }

        registerHandler("system.clipboard.copy") { p, cb -> (activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(android.content.ClipData.newPlainText("Copied", p.optString("text"))); cb(true, null) }
        registerHandler("system.clipboard.read") { _, cb -> cb((activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).primaryClip?.getItemAt(0)?.text?.toString() ?: "", null) }

        registerHandler("system.biometrics.authenticate") { p, cb -> mainHandler.post { val e = androidx.core.content.ContextCompat.getMainExecutor(activity); androidx.biometric.BiometricPrompt(activity, e, object : androidx.biometric.BiometricPrompt.AuthenticationCallback() { override fun onAuthenticationError(c: Int, s: CharSequence) { cb(false, s.toString()) }; override fun onAuthenticationSucceeded(r: androidx.biometric.BiometricPrompt.AuthenticationResult) { cb(true, null) }; override fun onAuthenticationFailed() { cb(false, "Failed") } }).authenticate(androidx.biometric.BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate").setSubtitle(p.optString("reason", "Please authenticate")).setNegativeButtonText("Cancel").build()) } }
    }
}