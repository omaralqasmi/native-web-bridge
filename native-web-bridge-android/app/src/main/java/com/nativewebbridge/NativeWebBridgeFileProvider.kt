package com.nativewebbridge

import androidx.core.content.FileProvider

// This empty class exists solely to prevent Manifest Merge crashes
// when imported into a host app that already has a FileProvider.
class NativeWebBridgeFileProvider : FileProvider()