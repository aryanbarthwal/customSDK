package com.example.mysdk

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface SdkWebSocketListener {
    fun onOpen()
    fun onMessage(message: String)
    fun onClose()
    fun onError(error: Throwable)
}

private val wsHttpClient = HttpClient {
    install(WebSockets)
}

internal object SdkWebSocketClient {
    fun connect(
        url: String,
        listener: SdkWebSocketListener,
    ): Job {
        val connectionJob = sdkStreamingScope.launch {
            try {
                wsHttpClient.webSocket(SdkRuntime.resolveUrl(url)) {
                    listener.onOpen()
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> listener.onMessage(frame.readText())
                            else -> {}
                        }
                    }
                    listener.onClose()
                }
            } catch (cancellation: CancellationException) {
                listener.onClose()
                throw cancellation
            } catch (throwable: Throwable) {
                listener.onError(throwable)
            }
        }
        return connectionJob
    }
}
