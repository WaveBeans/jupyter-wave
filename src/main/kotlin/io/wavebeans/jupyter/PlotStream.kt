package io.wavebeans.jupyter

import io.wavebeans.lib.Sample
import io.wavebeans.lib.TimeMeasure
import io.wavebeans.lib.stream.FiniteStream
import io.wavebeans.lib.stream.fft.FftSample
import jetbrains.letsPlot.geom.geom_area
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.geom.geom_tile
import jetbrains.letsPlot.ggsize
import jetbrains.letsPlot.lets_plot
import jetbrains.letsPlot.scale.scale_fill_gradient

fun FiniteStream<Sample>.plot(sampleRate: Float = 44100.0f): jetbrains.letsPlot.intern.Plot {
    val dataFrame = this.dataFrame(sampleRate)
    return lets_plot { x = "time, ms"; y = "value" } +
            geom_line(dataFrame, color = "blue")
}

fun FiniteStream<FftSample>.plot(
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Int, Int> = 0 to (sampleRate.toInt() / 2),
        gsize: Pair<Int, Int> = 1000 to 600
): jetbrains.letsPlot.intern.Plot {
    val dataFrame = this.dataFrame(sampleRate, freqCutOff)

    return lets_plot(dataFrame) { x = "time"; y = "freq"; fill = "value" } +
            ggsize(gsize.first, gsize.second) +
            geom_tile() +
            scale_fill_gradient(low = "light_green", high = "red")
}
fun FiniteStream<FftSample>.plot(
        offset: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Int, Int> = 0 to (sampleRate.toInt() / 2)
): jetbrains.letsPlot.intern.Plot {

    val dataFrame = this.dataFrame(offset, sampleRate, freqCutOff)

    return lets_plot { x = "frequency, Hz"; y = "value, dB" } +
            geom_line(dataFrame, color = "blue")
}