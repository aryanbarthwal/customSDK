package com.example.mysdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SseEventAccumulatorTest {
    @Test
    fun parsesStructuredEvent() {
        val accumulator = SseEventAccumulator()

        assertNull(accumulator.consume("event: update"))
        assertNull(accumulator.consume("id: 42"))
        assertNull(accumulator.consume("retry: 1500"))
        assertNull(accumulator.consume("data: first line"))
        assertNull(accumulator.consume("data: second line"))

        val event = accumulator.consume("")

        assertEquals(
            SdkServerSentEvent(
                data = "first line\nsecond line",
                event = "update",
                id = "42",
                retryMillis = 1500L,
            ),
            event,
        )
    }

    @Test
    fun ignoresCommentAndNonDataBlocks() {
        val accumulator = SseEventAccumulator()

        assertNull(accumulator.consume(": keep-alive"))
        assertNull(accumulator.consume("id: 9"))
        assertNull(accumulator.consume(""))
        assertNull(accumulator.flush())
    }

    @Test
    fun defaultsEventNameToMessage() {
        val accumulator = SseEventAccumulator()

        assertNull(accumulator.consume("data: hello"))

        assertEquals(
            SdkServerSentEvent(
                data = "hello",
                event = "message",
            ),
            accumulator.flush(),
        )
    }
}
