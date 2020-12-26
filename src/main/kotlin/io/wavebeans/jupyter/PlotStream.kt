package io.wavebeans.jupyter

import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.TimeMeasure
import io.wavebeans.lib.stream.FiniteStream
import io.wavebeans.lib.stream.fft.FftSample
import jetbrains.letsPlot.coord_fixed
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.geom.geom_tile
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.layer.SamplingOptions
import jetbrains.letsPlot.lets_plot
import jetbrains.letsPlot.sampling.sampling_pick
import jetbrains.letsPlot.scale.scale_fill_gradient

fun BeanStream<Sample>.plot(
        length: TimeMeasure,
        sampleRate: Float = 44100.0f,
): Plot =
        plotSample(dataFrame(length = length, sampleRate = sampleRate))

fun FiniteStream<Sample>.plot(sampleRate: Float = 44100.0f): Plot =
        plotSample(dataFrame(sampleRate = sampleRate))

fun BeanStream<FftSample>.plot(
        length: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0),
        sampling: SamplingOptions = sampling_pick(20000),
        timeThinningFunction: (FftSample) -> Boolean = { true },
        frequencyThinningFunction: (Int, Double) -> Boolean = { _, _ -> true },
): Plot = plotFftSample(
        dataFrame(length, sampleRate, freqCutOff, timeThinningFunction, frequencyThinningFunction),
        sampling
)

fun FiniteStream<FftSample>.plot(
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0),
        sampling: SamplingOptions = sampling_pick(20000),
        timeThinningFunction: (FftSample) -> Boolean = { true },
        frequencyThinningFunction: (Int, Double) -> Boolean = { _, _ -> true },
): Plot = plotFftSample(
        dataFrame(sampleRate, freqCutOff, timeThinningFunction, frequencyThinningFunction),
        sampling
)

fun BeanStream<FftSample>.plotAt(
        offset: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0),
): Plot = plotSingularFftSample(dataFrameAt(offset, sampleRate, freqCutOff))

private fun plotSingularFftSample(dataFrame: WbDataFrame) =
        lets_plot { x = COL_FREQUENCY; y = COL_VALUE } +
                geom_line(dataFrame, color = "blue")

private fun plotSample(dataFrame: WbDataFrame) =
        lets_plot { x = COL_TIME; y = COL_VALUE } +
                geom_line(dataFrame, color = "blue")

@Suppress("UNCHECKED_CAST")
private fun plotFftSample(
        dataFrame: WbDataFrame,
        sampling: SamplingOptions,
): Plot {
    val maxTime = (dataFrame[COL_TIME] as List<Double>).maxOrNull()!!
    val maxFreq = (dataFrame[COL_FREQUENCY] as List<Double>).maxOrNull()!!
    val minFreq = (dataFrame[COL_FREQUENCY] as List<Double>).minOrNull()!!
    val r = maxTime / (maxFreq - minFreq)
    return lets_plot(dataFrame) { x = COL_TIME; y = COL_FREQUENCY; fill = COL_VALUE } +
            geom_tile(sampling = sampling) +
            coord_fixed(ratio = r) +
            scale_fill_gradient(low = "light_green", high = "red")
}
