package com.example.mysdk

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

internal val sdkStreamingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

data class SdkServerSentEvent(
    val data: String,
    val event: String = "message",
    val id: String? = null,
    val retryMillis: Long? = null,
)

interface SdkSseListener {
    fun onOpen()

    fun onEvent(event: SdkServerSentEvent)

    fun onClosed()

    fun onError(message: String)
}

class SdkSseConnection internal constructor(
    private val job: Job,
) {
    val isActive: Boolean
        get() = job.isActive

    fun cancel() {
        job.cancel()
    }
}

internal object SdkSseClient {
    fun connect(
        url: String,
        headers: Map<String, String>,
        listener: SdkSseListener,
    ): SdkSseConnection {
        val config = SdkRuntime.requireConfig()
        val connectionJob = sdkStreamingScope.launch {
            try {
                sdkHttpClient.prepareGet(SdkRuntime.resolveUrl(url)) {
                    header(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
                    config.apiKey?.let { apiKey ->
                        header("X-API-Key", apiKey)
                    }
                    headers.forEach { (name, value) ->
                        header(name, value)
                    }
                }.execute { response ->
                    if (!response.status.isSuccess()) {
                        throw ClientRequestException(response, "SSE request failed with ${response.status.value}")
                    }

                    val contentType = response.headers[HttpHeaders.ContentType]
                    if (contentType?.startsWith(ContentType.Text.EventStream.toString()) != true) {
                        throw IllegalStateException(
                            "Expected '${ContentType.Text.EventStream}' response but received '${contentType ?: "unknown"}'",
                        )
                    }

                    listener.onOpen()
                    response.bodyAsChannel().collectServerSentEvents(listener::onEvent)
                    listener.onClosed()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                listener.onError(throwable.message ?: "SSE connection failed")
            }
        }

        return SdkSseConnection(connectionJob)
    }

    fun dispose() {
        sdkStreamingScope.cancel()
    }
}

private suspend fun ByteReadChannel.collectServerSentEvents(
    onEvent: (SdkServerSentEvent) -> Unit,
) {
    val accumulator = SseEventAccumulator()

    while (true) {
        val line = readUTF8Line() ?: break
        accumulator.consume(line)?.let(onEvent)
    }

    accumulator.flush()?.let(onEvent)
}

internal class SseEventAccumulator {
    private var eventName: String? = null
    private var eventId: String? = null
    private var retryMillis: Long? = null
    private val dataLines = mutableListOf<String>()

    fun consume(line: String): SdkServerSentEvent? {
        if (line.isEmpty()) {
            return flush()
        }

        if (line.startsWith(":")) {
            return null
        }

        val separatorIndex = line.indexOf(':')
        val field = if (separatorIndex >= 0) {
            line.substring(0, separatorIndex)
        } else {
            line
        }
        val rawValue = if (separatorIndex >= 0) {
            line.substring(separatorIndex + 1).removePrefix(" ")
        } else {
            ""
        }

        when (field) {
            "data" -> dataLines += rawValue
            "event" -> eventName = rawValue.ifEmpty { null }
            "id" -> eventId = rawValue
            "retry" -> retryMillis = rawValue.toLongOrNull()
        }

        return null
    }

    fun flush(): SdkServerSentEvent? {
        if (dataLines.isEmpty()) {
            clear()
            return null
        }

        val event = SdkServerSentEvent(
            data = dataLines.joinToString(separator = "\n"),
            event = eventName ?: "message",
            id = eventId,
            retryMillis = retryMillis,
        )
        clear()
        return event
    }

    private fun clear() {
        eventName = null
        eventId = null
        retryMillis = null
        dataLines.clear()
    }
}
