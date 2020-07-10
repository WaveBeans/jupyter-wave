package io.wavebeans.jupyter

import io.wavebeans.lib.TimeMeasure

class TimeseriesList<T : Any>(
        private val granularValueInMs: Long = 60000,
        private val accumulator: (T, T) -> T
) {

    private val timeseries = ArrayDeque<Pair<Long, T>>()

    private var lastValueTimestamp: Long = -1

    private var lastValue: T? = null

    @Synchronized
    fun reset() {
        timeseries.clear()
        lastValueTimestamp = -1
        lastValue = null
    }

    @Synchronized
    fun append(v: T, now: Long = System.currentTimeMillis()): Boolean {
        if(now < lastValueTimestamp) return false

        val lv = lastValue
        if (lv == null) {
            lastValue = v
        } else {
            val lastBucket = lastValueTimestamp / granularValueInMs
            val currentBucket = now / granularValueInMs
            lastValue = if (currentBucket > lastBucket) {
                timeseries.addLast(Pair(lastBucket * granularValueInMs, lv))
                v
            } else {
                accumulator(lv, v)
            }
        }
        lastValueTimestamp = now
        return true
    }

    @Synchronized
    fun leaveOnlyLast(interval: TimeMeasure, now: Long = System.currentTimeMillis()) {
        val lastMarker = now - interval.asNanoseconds() / 1_000_000
        timeseries.removeIf { it.first < lastMarker }
        if (lastValueTimestamp < lastMarker) {
            lastValueTimestamp = -1
            lastValue = null
        }
    }

    @Synchronized
    fun inLast(interval: TimeMeasure, now: Long = System.currentTimeMillis()): T? {
        val lastMarker = now - interval.asNanoseconds() / 1_000_000
        val v = timeseries.asSequence()
                .dropWhile { it.first < lastMarker }
                .map { it.second }
                .reduceOrNull { acc, v -> accumulator(acc, v) }
        return when {
            lastValue != null && lastMarker < lastValueTimestamp && v != null -> accumulator(v, lastValue!!)
            v == null -> lastValue
            else -> v
        }
    }
}