package io.wavebeans.jupyter

import io.wavebeans.lib.Sample
import io.wavebeans.lib.TimeMeasure
import io.wavebeans.lib.stream.FiniteStream
import io.wavebeans.lib.stream.fft.FftSample

typealias WbDataFrame = Map<String, List<Any>>

fun FiniteStream<Sample>.dataFrame(
        sampleRate: Float = 44100.0f,
): WbDataFrame {
    val values = this.asSequence(sampleRate).toList()
    return mapOf(
            "time, ms" to values.indices.asSequence().map { it / sampleRate * 1000.0 }.toList(),
            "value" to values
    )
}

fun FiniteStream<FftSample>.dataFrame(
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Int, Int> = 0 to (sampleRate.toInt() / 2)
): WbDataFrame {

    val table = asSequence(sampleRate)
            .map { fftSample ->
                fftSample.magnitude()
                        .zip(fftSample.frequency())
                        .filter { it.second >= freqCutOff.first && it.second <= freqCutOff.second }
                        .map {
                            arrayOf(
                                    fftSample.time() / 1e+6, // to milliseconds
                                    it.second,
                                    it.first
                            )
                        }
            }
            .flatMap { it }
            .toList()

    return mapOf(
            "time" to table.map { it[0] },
            "freq" to table.map { it[1] },
            "value" to table.map { it[2] }
    )
}

fun FiniteStream<FftSample>.dataFrame(
        offset: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Int, Int> = 0 to (sampleRate.toInt() / 2)
): WbDataFrame {
    val table = asSequence(sampleRate)
            .drop((offset.ns() / 1e9 / sampleRate).toInt())
            .take(1)
            .map { fftSample ->
                fftSample.magnitude()
                        .zip(fftSample.frequency())
                        .filter { it.second >= freqCutOff.first && it.second <= freqCutOff.second }
                        .toList()
            }
            .first()

    return mapOf(
            "value, dB" to table.map { it.first },
            "frequency, Hz" to table.map { it.second }
    )
}