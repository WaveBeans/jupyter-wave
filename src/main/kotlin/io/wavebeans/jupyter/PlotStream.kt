package io.wavebeans.jupyter

import io.wavebeans.lib.Sample
import io.wavebeans.lib.stream.FiniteStream
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.lets_plot

fun FiniteStream<Sample>.plot(sampleRate: Float = 44100.0f): jetbrains.letsPlot.intern.Plot {
    val values = this.asSequence(sampleRate).toList()
    val dataFrame = mapOf(
            "time, ms" to values.indices.asSequence().map { it / sampleRate * 1000.0} .toList(),
            "value" to values
    )
    return lets_plot { x = "time, ms"; y = "value" } +
            geom_line(dataFrame, color = "blue")
}