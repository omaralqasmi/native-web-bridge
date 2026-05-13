package com.nativewebbridge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

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
            if (resultCode == Activity.RESULT_OK) {
                activeCallback?.invoke(data, null)
            } else {
                activeCallback?.invoke(null, "User cancelled")
            }
            activeCallback = null
        }
    }
}