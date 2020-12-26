package io.wavebeans.jupyter

import io.wavebeans.lib.*
import io.wavebeans.lib.stream.FiniteStream
import io.wavebeans.lib.stream.fft.FftSample
import io.wavebeans.lib.stream.trim
import java.util.concurrent.TimeUnit

fun BeanStream<Sample>.dataFrame(
        length: TimeMeasure,
        timeShift: Double = 0.0,
        sampleRate: Float = 44100.0f,
): WbDataFrame {
    return this.trim(length.ns(), TimeUnit.NANOSECONDS).dataFrame(timeShift, sampleRate)
}

fun FiniteStream<Sample>.dataFrame(
        timeShift: Double = 0.0,
        sampleRate: Float = 44100.0f,
): WbDataFrame {
    return this.dataFrame(timeShift, sampleRate) { it.asDouble() }
}

@JvmName("dataFrameOfAny")
fun <T : Any> BeanStream<T>.dataFrame(
        length: TimeMeasure,
        timeShift: Double = 0.0,
        sampleRate: Float = 44100.0f,
        mapper: (T) -> Any = { it },
): WbDataFrame {
    return this.trim(length.ns(), TimeUnit.NANOSECONDS).dataFrame(timeShift, sampleRate, mapper)
}

@JvmName("dataFrameOfAny")
fun <T : Any> FiniteStream<T>.dataFrame(
        timeShift: Double = 0.0,
        sampleRate: Float = 44100.0f,
        mapper: (T) -> Any = { it },
): WbDataFrame {
    require(sampleRate > 0.0f) { "sampleRate must be greater than 0, but $sampleRate found" }
    val values = this.asSequence(sampleRate).toList()
    return mapOf(
            COL_TIME to values.indices.asSequence().map { timeShift + it / sampleRate.toDouble() }.toList(),
            COL_VALUE to values.map(mapper)
    )
}

@JvmName("dataFrameOfFftSample")
fun BeanStream<FftSample>.dataFrame(
        length: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0),
        timeThinningFunction: (FftSample) -> Boolean = { true },
        frequencyThinningFunction: (Int, Double) -> Boolean = { _, _ -> true },
): WbDataFrame {
    return this.trim(length.ns(), TimeUnit.NANOSECONDS).dataFrame(
            sampleRate,
            freqCutOff,
            timeThinningFunction,
            frequencyThinningFunction
    )
}

@JvmName("dataFrameOfFftSample")
fun FiniteStream<FftSample>.dataFrame(
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0),
        timeThinningFunction: (FftSample) -> Boolean = { true },
        frequencyThinningFunction: (Int, Double) -> Boolean = { _, _ -> true },
): WbDataFrame {
    require(sampleRate > 0.0f) { "sampleRate must be greater than 0, but $sampleRate found" }
    require(freqCutOff.first.toDouble() >= 0) {
        "freqCutOff low boundary must be greater or equal to 0, " +
                "but ${freqCutOff.first} found"
    }
    require(freqCutOff.second.toDouble() > freqCutOff.first.toDouble()) {
        "freqCutOff high boundary must be greater than low, " +
                "but ${freqCutOff.second} found while the low is ${freqCutOff.first}"
    }
    val table = asSequence(sampleRate)
            .filter(timeThinningFunction)
            .map { fftSample ->
                fftSample.magnitude()
                        .zip(fftSample.frequency().filterIndexed(frequencyThinningFunction))
                        .filter { it.second >= freqCutOff.first.toDouble() && it.second <= freqCutOff.second.toDouble() }
                        .map {
                            arrayOf(
                                    fftSample.time() / 1e+9, // to seconds
                                    it.second,
                                    it.first
                            )
                        }
            }
            .flatMap { it }
            .toList()

    return mapOf(
            COL_TIME to table.map { it[0] },
            COL_FREQUENCY to table.map { it[1] },
            COL_VALUE to table.map { it[2] }
    )
}

@JvmName("dataFrameAtOfFftSample")
fun BeanStream<FftSample>.dataFrameAt(
        offset: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate.toInt() / 2),
): WbDataFrame {
    val table = asSequence(sampleRate)
            .drop((offset.ns() / 1e9 / sampleRate).toInt())
            .take(1)
            .map { fftSample ->
                fftSample.magnitude()
                        .zip(fftSample.frequency())
                        .filter { it.second >= freqCutOff.first.toDouble() && it.second <= freqCutOff.second.toDouble() }
                        .toList()
            }
            .first()

    return mapOf(
            COL_VALUE to table.map { it.first },
            COL_FREQUENCY to table.map { it.second }
    )
}