package io.wavebeans.jupyter

import io.wavebeans.lib.m
import io.wavebeans.metrics.*
import io.wavebeans.metrics.collector.TimeseriesList
import mu.KotlinLogging

/**
 * Tracks activity on the table, the table must be created in the same JVM instance,
 * hence is used to run within [io.wavebeans.execution.SingleThreadedOverseer].
 *
 * Implemented as [MetricConnector] and listens metrics from [MetricService].
 */
class LocalTableActivityTracker(private val tableName: String) : MetricConnector {

    companion object {
        private val log = KotlinLogging.logger { }

        fun createAndRegister(tableName: String): LocalTableActivityTracker {
            val tracker = LocalTableActivityTracker(tableName)
            MetricService.registerConnector(tracker)

            return tracker
        }
    }

    private val incomingRequest = TimeseriesList<Double> { a, b -> a + b }
    private val sentBytes = TimeseriesList<Double> { a, b -> a + b }
    private var hasStarted = false

    fun isStillActive(): Boolean {
        sentBytes.leaveOnlyLast(1.m.ms())
        incomingRequest.leaveOnlyLast(1.m.ms())

        val sentBytesInLastMinute = sentBytes.inLast(1.m.ms()) ?: 0.0
        val incomingRequestsInLastMinute = incomingRequest.inLast(1.m.ms()) ?: 0.0

        log.debug {
            "[Table=$tableName] Checking last values: sentBytesInLastMinute=$sentBytesInLastMinute, " +
                    "incomingRequestsInLastMinute=$incomingRequestsInLastMinute"
        }
        if (sentBytesInLastMinute > 0 && !hasStarted) {
            log.debug { "[Table=$tableName] Consumption started" }
            hasStarted = true
        } else if (sentBytesInLastMinute < 1e6 && incomingRequestsInLastMinute < 1e6 && hasStarted) {
            hasStarted = false
            log.debug { "[Table=$tableName] Consumption stopped" }
            return false
        }
        return true
    }


    override fun increment(metricObject: CounterMetricObject, delta: Double) {
        if (metricObject.tags[tableTag] == tableName) {
            if (metricObject.isLike(audioStreamRequestMetric)) {
                incomingRequest.append(delta)
            }
            if (metricObject.isLike(audioStreamBytesSentMetric)) {
                sentBytes.append(delta)
            }
        }
    }

    override fun decrement(metricObject: CounterMetricObject, delta: Double) {}

    override fun gauge(metricObject: GaugeMetricObject, value: Double) {}

    override fun gaugeDelta(metricObject: GaugeMetricObject, delta: Double) {}

    override fun time(metricObject: TimeMetricObject, valueInMs: Long) {}
}