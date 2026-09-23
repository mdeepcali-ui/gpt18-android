package com.gptplus18.app.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.IOException

/**
 * RequestBody يبلّغ عن progress الرفع لحظياً
 */
class CountingRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (bytesWritten: Long, contentLength: Long) -> Unit,
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        val countingSink = CountingSink(sink, totalLength, onProgress)
        val bufferedSink = countingSink.buffer()
        try {
            delegate.writeTo(bufferedSink)
            bufferedSink.flush()
        } finally {
            bufferedSink.close()
        }
    }

    private class CountingSink(
        sink: Sink,
        private val totalLength: Long,
        private val onProgress: (Long, Long) -> Unit,
    ) : ForwardingSink(sink) {
        private var bytesWritten = 0L
        private var lastReported = 0L

        override fun write(source: okio.Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            // نبلّغ فقط عند تغيّر 1% أو 64KB لتقليل التحديثات
            val threshold = maxOf(totalLength / 100, 64L * 1024)
            if (bytesWritten - lastReported >= threshold || bytesWritten == totalLength) {
                lastReported = bytesWritten
                try {
                    onProgress(bytesWritten, totalLength)
                } catch (_: Exception) { }
            }
        }
    }
}
