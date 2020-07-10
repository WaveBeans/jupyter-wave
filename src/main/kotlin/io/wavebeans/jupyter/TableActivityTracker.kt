package io.wavebeans.jupyter

import io.wavebeans.execution.metrics.MetricConnector
import io.wavebeans.execution.metrics.MetricObject
import io.wavebeans.http.AudioService
import io.wavebeans.lib.m
import mu.KotlinLogging

class TableActivityTracker(private val tableName: String) : MetricConnector {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val incomingRequest = TimeseriesList<Long> { a, b -> a + b }
    private val sentBytes = TimeseriesList<Long> { a, b -> a + b }
    private var hasStarted = false

    fun isStillActive(): Boolean {
        sentBytes.leaveOnlyLast(1.m)
        incomingRequest.leaveOnlyLast(1.m)

        val sentBytesInLastMinute = sentBytes.inLast(1.m) ?: 0L
        val incomingRequestsInLastMinute = incomingRequest.inLast(1.m) ?: 0L

        log.debug {
            "[Table=$tableName] Checking last values: sentBytesInLastMinute=$sentBytesInLastMinute, " +
                    "incomingRequestsInLastMinute=$incomingRequestsInLastMinute"
        }
        if (sentBytesInLastMinute > 0 && !hasStarted) {
            log.debug { "[Table=$tableName] Consumption started" }
            hasStarted = true
        } else if (sentBytesInLastMinute == 0L && incomingRequestsInLastMinute == 0L && hasStarted) {
            hasStarted = false
            log.debug { "[Table=$tableName] Consumption stopped" }
            return false
        }
        return true
    }


    override fun decrement(metricObject: MetricObject, delta: Long) {}

    override fun gauge(metricObject: MetricObject, value: Double) {}

    override fun gauge(metricObject: MetricObject, value: Long) {}

    override fun gaugeDelta(metricObject: MetricObject, delta: Double) {}

    override fun gaugeDelta(metricObject: MetricObject, delta: Long) {}

    override fun increment(metricObject: MetricObject, delta: Long) {
        if (metricObject.tags[AudioService.tableTag] == tableName) {
            if (
                    metricObject.component == AudioService.audioStreamRequest.component &&
                    metricObject.name == AudioService.audioStreamRequest.name
            ) {
                incomingRequest.append(delta)
            }
            if (
                    metricObject.component == AudioService.audioStreamBytesSent.component &&
                    metricObject.name == AudioService.audioStreamBytesSent.name
            ) {
                sentBytes.append(delta)
            }
        }
    }

    override fun time(metricObject: MetricObject, valueInMs: Long) {}
}