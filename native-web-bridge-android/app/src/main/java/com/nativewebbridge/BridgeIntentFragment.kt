package com.nativewebbridge

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class BridgeIntentFragment : Fragment() {
    private var intentCallback: ((Intent?, String?) -> Unit)? = null
    private var permissionCallback: ((Boolean) -> Unit)? = null

    // Modern Intent Launcher
    private val intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            intentCallback?.invoke(result.data, null)
        } else {
            intentCallback?.invoke(null, "User cancelled")
        }
        intentCallback = null
    }

    // Modern Permission Requester
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionCallback?.invoke(isGranted)
        permissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun launchIntent(intent: Intent, cb: (Intent?, String?) -> Unit) {
        intentCallback = cb
        intentLauncher.launch(intent)
    }

    fun requestPermission(permission: String, cb: (Boolean) -> Unit) {
        permissionCallback = cb
        permissionLauncher.launch(permission)
    }
}