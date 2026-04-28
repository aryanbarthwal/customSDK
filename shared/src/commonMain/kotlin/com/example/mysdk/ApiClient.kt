package com.example.mysdk

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val sdkApiKeyHeader = "X-API-Key"

internal val sdkHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }
}

@Serializable
data class DemoPost(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String,
)

internal object SdkApi {
    suspend fun fetchDemoPost(): DemoPost {
        val config = SdkRuntime.requireConfig()
        return sdkHttpClient.get(SdkRuntime.resolveUrl("/posts/1")) {
            config.apiKey?.let { apiKey ->
                header(sdkApiKeyHeader, apiKey)
            }
        }.body()
    }
}
