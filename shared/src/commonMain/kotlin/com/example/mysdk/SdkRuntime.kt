package com.example.mysdk

internal object SdkRuntime {
    private var sdkConfig: SdkConfig? = null

    fun initialize(config: SdkConfig) {
        val normalizedBaseUrl = config.baseUrl.trim().trimEnd('/')
        require(normalizedBaseUrl.isNotEmpty()) {
            "SdkConfig.baseUrl must not be blank."
        }

        sdkConfig = config.copy(
            baseUrl = normalizedBaseUrl,
            apiKey = config.apiKey?.trim()?.takeIf(String::isNotEmpty),
        )
    }

    fun requireConfig(): SdkConfig {
        return checkNotNull(sdkConfig) {
            "MySdk.init(config) must be called before using the SDK."
        }
    }

    fun resolveUrl(url: String): String {
        val normalizedUrl = url.trim()
        require(normalizedUrl.isNotEmpty()) {
            "URL must not be blank."
        }

        return if (
            normalizedUrl.startsWith("http://") ||
            normalizedUrl.startsWith("https://")
        ) {
            normalizedUrl
        } else {
            val baseUrl = requireConfig().baseUrl
            if (normalizedUrl.startsWith("/")) "$baseUrl$normalizedUrl" else "$baseUrl/$normalizedUrl"
        }
    }
}
