package com.example.mysdk

object MySdk {
    fun init(config: SdkConfig) {
        SdkRuntime.initialize(config)
        SdkStore.initialize()
    }

    fun openSdk() {
        SdkRuntime.requireConfig()
        PlatformSdkBridge.openSdkScreen()
    }

    fun closeSdk() {
        PlatformSdkBridge.closeSdkScreen()
    }

    fun isSdkVisible(): Boolean {
        return PlatformSdkBridge.isSdkVisible()
    }

    suspend fun fetchDemoPost(): DemoPost {
        SdkRuntime.requireConfig()
        return SdkApi.fetchDemoPost()
    }

    suspend fun getSavedNames(): List<NameEntity> {
        SdkRuntime.requireConfig()
        return SdkStore.names.getAllNames()
    }

    suspend fun insertName(name: String) {
        SdkRuntime.requireConfig()
        SdkStore.names.insertName(name)
    }

    fun connectToServerSentEvents(
        url: String,
        listener: SdkSseListener,
        headers: Map<String, String> = emptyMap(),
    ): SdkSseConnection {
        SdkRuntime.requireConfig()
        return SdkSseClient.connect(
            url = url,
            headers = headers,
            listener = listener,
        )
    }
}

expect object PlatformSdkBridge {
    fun openSdkScreen()
    fun closeSdkScreen()
    fun isSdkVisible(): Boolean
}
