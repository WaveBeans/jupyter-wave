package io.wavebeans.jupyter

import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.TimeMeasure
import io.wavebeans.lib.stream.FiniteStream
import io.wavebeans.lib.stream.fft.FftSample
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.geom.geom_tile
import jetbrains.letsPlot.lets_plot
import jetbrains.letsPlot.scale.scale_fill_gradient

fun BeanStream<Sample>.plot(
        length: TimeMeasure,
        sampleRate: Float = 44100.0f
): jetbrains.letsPlot.intern.Plot =
        plotSample(dataFrame(length = length, sampleRate = sampleRate))

fun FiniteStream<Sample>.plot(sampleRate: Float = 44100.0f): jetbrains.letsPlot.intern.Plot =
        plotSample(dataFrame(sampleRate = sampleRate))

fun BeanStream<FftSample>.plot(
        length: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0)
): jetbrains.letsPlot.intern.Plot = plotFftSample(dataFrame(length, sampleRate, freqCutOff))

fun FiniteStream<FftSample>.plot(
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0)
): jetbrains.letsPlot.intern.Plot = plotFftSample(dataFrame(sampleRate, freqCutOff))

fun BeanStream<FftSample>.plotAt(
        offset: TimeMeasure,
        sampleRate: Float = 44100.0f,
        freqCutOff: Pair<Number, Number> = 0 to (sampleRate / 2.0)
): jetbrains.letsPlot.intern.Plot = plotSingularFftSample(dataFrameAt(offset, sampleRate, freqCutOff))

private fun plotSingularFftSample(dataFrame: WbDataFrame) =
        lets_plot { x = COL_FREQUENCY; y = COL_VALUE } +
                geom_line(dataFrame, color = "blue")

private fun plotSample(dataFrame: WbDataFrame) =
        lets_plot { x = COL_TIME; y = COL_VALUE } +
                geom_line(dataFrame, color = "blue")

private fun plotFftSample(dataFrame: WbDataFrame) =
        lets_plot(dataFrame) { x = COL_TIME; y = COL_FREQUENCY; fill = COL_VALUE } +
                geom_tile() +
                scale_fill_gradient(low = "light_green", high = "red")
