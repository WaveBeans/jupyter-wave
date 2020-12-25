package io.wavebeans.jupyter

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import io.wavebeans.lib.io.sine
import io.wavebeans.lib.ms
import io.wavebeans.lib.stream.fft.fft
import io.wavebeans.lib.stream.trim
import io.wavebeans.lib.stream.window.window
import jetbrains.letsPlot.geom.geom_line
import jetbrains.letsPlot.geom.geom_tile
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.layer.LayerBase
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PlotStreamSpec : Spek({
    describe("Samples") {
        val input = 440.sine()
        val columnsMapping = mapOf("x" to COL_TIME, "y" to COL_VALUE)

        it("should plot infinite stream") {
            assertThat(input.plot(10.ms, sampleRate = 1000.0f)).all {
                mapping().isEqualTo(columnsMapping)
                features().all {
                    size().isEqualTo(1)
                    index(0).isInstanceOf(geom_line::class)
                            .data().isNotNull().all {
                                size().isEqualTo(2)
                                value<List<Any>>(COL_TIME).isNotNull().size().isEqualTo(10)
                                value<List<Any>>(COL_VALUE).isNotNull().size().isEqualTo(10)
                            }
                }
            }
        }
        it("should plot finite stream") {
            assertThat(input.trim(10).plot(sampleRate = 1000.0f)).all {
                mapping().isEqualTo(columnsMapping)
                features().all {
                    size().isEqualTo(1)
                    index(0).isInstanceOf(geom_line::class)
                            .data().isNotNull().all {
                                size().isEqualTo(2)
                                value<List<Any>>(COL_TIME).isNotNull().size().isEqualTo(10)
                                value<List<Any>>(COL_VALUE).isNotNull().size().isEqualTo(10)
                            }
                }
            }
        }
    }
    describe("FFT Samples sequence") {
        val input = 440.sine().window(5).fft(8)
        val columnsMapping = mapOf("x" to COL_TIME, "y" to COL_FREQUENCY, "fill" to COL_VALUE)

        it("should plot infinite stream") {
            assertThat(input.plot(10.ms, sampleRate = 1000.0f)).all {
                mapping().isEqualTo(columnsMapping)
                data().isNotNull().all {
                    size().isEqualTo(3)
                    value<List<Any>>(COL_TIME).isNotNull().size().isEqualTo(8)
                    value<List<Any>>(COL_FREQUENCY).isNotNull().size().isEqualTo(8)
                    value<List<Any>>(COL_VALUE).isNotNull().size().isEqualTo(8)
                }
                features().all {
                    size().isGreaterThanOrEqualTo(1)
                    index(0).isInstanceOf(geom_tile::class)
                }
            }
        }
        it("should plot finite stream") {
            assertThat(input.trim(10).plot(sampleRate = 1000.0f)).all {
                mapping().isEqualTo(columnsMapping)
                data().isNotNull().all {
                    size().isEqualTo(3)
                    value<List<Any>>(COL_TIME).isNotNull().size().isEqualTo(8)
                    value<List<Any>>(COL_FREQUENCY).isNotNull().size().isEqualTo(8)
                    value<List<Any>>(COL_VALUE).isNotNull().size().isEqualTo(8)
                }
                features().all {
                    size().isGreaterThanOrEqualTo(1)
                    index(0).isInstanceOf(geom_tile::class)
                }
            }
        }
    }
    describe("FFT Sample") {
        val input = 440.sine().window(5).fft(8)
        val columnsMapping = mapOf("x" to COL_FREQUENCY, "y" to COL_VALUE)

        it("should plot the stream") {
            assertThat(input.plotAt(10.ms, sampleRate = 1000.0f)).all {
                mapping().isEqualTo(columnsMapping)
                features().all {
                    size().isGreaterThanOrEqualTo(1)
                    index(0).isInstanceOf(geom_line::class)
                            .data().isNotNull().all {
                                size().isEqualTo(2)
                                value<List<Any>>(COL_FREQUENCY).isNotNull().size().isEqualTo(4)
                                value<List<Any>>(COL_VALUE).isNotNull().size().isEqualTo(4)
                            }
                }
            }
        }
    }
})

@Suppress("UNCHECKED_CAST")
private fun <T> Assert<Map<*, *>>.value(key: Any) = prop("@$key") { it[key] as T? }

private fun Assert<LayerBase>.data() = prop("data") { it.data }

@JvmName("dataPlot")
private fun Assert<Plot>.data() = prop("data") { it.data }

@Suppress("UNCHECKED_CAST")
private fun Assert<Plot>.features() = prop("feature") { it.features as List<LayerBase> }

private fun Assert<Plot>.mapping() = prop("mapping") { it.mapping.map }