package com.example.mysdk

actual object PlatformSdkBridge {
    private var sdkVisible = false

    actual fun openSdkScreen() {
        sdkVisible = true
    }

    actual fun closeSdkScreen() {
        sdkVisible = false
    }

    actual fun isSdkVisible(): Boolean = sdkVisible
}
