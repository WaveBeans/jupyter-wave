package io.wavebeans.jupyter

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import assertk.catch
import io.wavebeans.lib.io.input
import io.wavebeans.lib.io.sine
import io.wavebeans.lib.ms
import io.wavebeans.lib.sampleOf
import io.wavebeans.lib.stream.fft.fft
import io.wavebeans.lib.stream.trim
import io.wavebeans.lib.stream.window.window
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object DataFrameStreamSpec : Spek({
    describe("Sample") {
        val input = input { (i, _) -> if (i < 1000) sampleOf(i * 0.01) else null }

        it("should create a dataframe of infinite (by interface) stream (timeShift=0.0s)") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should create a dataframe of infinite (by interface) stream with shift") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f, timeShift = 1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(1.0, 1.001, 1.002, 1.003, 1.004),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should create a dataframe of infinite (by interface) stream with negative shift") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f, timeShift = -1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(-1.0, -0.999, -0.998, -0.997, -0.996),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }

        it("should create a dataframe of finite (by interface) stream") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should create a dataframe of finite (by interface) stream with shift") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f, timeShift = 1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(1.0, 1.001, 1.002, 1.003, 1.004),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should create a dataframe of finite (by interface) stream with negative shift") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f, timeShift = -1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(-1.0, -0.999, -0.998, -0.997, -0.996),
                    COL_VALUE to listOf(0.00, 0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should fail if sample rate is negative while attempting to create a dataframe of infinite stream") {
            assertThat(catch { input.dataFrame(1.ms, sampleRate = -1.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
        }
        it("should fail if sample rate is zero while attempting to create a dataframe of infinite stream") {
            assertThat(catch { input.dataFrame(1.ms, sampleRate = 0.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
        }
        it("should fail if sample rate is negative while attempting to create a dataframe of finite stream") {
            assertThat(catch { input.trim(1).dataFrame(sampleRate = -1.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
        }
        it("should fail if sample rate is zero while attempting to create a dataframe of finite stream") {
            assertThat(catch { input.trim(1).dataFrame(sampleRate = 0.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
        }
    }

    describe("Int with default mapper") {
        val input = input { (i, _) -> if (i < 1000) i.toInt() else null }

        it("should create a dataframe of infinite (by interface) stream (timeShift=0.0s)") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should create a dataframe of infinite (by interface) stream with shift") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f, timeShift = 1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(1.0, 1.001, 1.002, 1.003, 1.004),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should create a dataframe of infinite (by interface) stream with negative shift") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f, timeShift = -1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(-1.0, -0.999, -0.998, -0.997, -0.996),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should create a dataframe of finite (by interface) stream") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should create a dataframe of finite (by interface) stream with shift") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f, timeShift = 1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(1.0, 1.001, 1.002, 1.003, 1.004),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should create a dataframe of finite (by interface) stream with negative shift") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f, timeShift = -1.0)).isEqualTo(mapOf(
                    COL_TIME to listOf(-1.0, -0.999, -0.998, -0.997, -0.996),
                    COL_VALUE to listOf(0, 1, 2, 3, 4)
            ))
        }
        it("should fail if sample rate is negative while attempting to create a dataframe of infinite stream") {
            assertThat(catch { input.dataFrame(1.ms, sampleRate = -1.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
        }
        it("should fail if sample rate is zero while attempting to create a dataframe of infinite stream") {
            assertThat(catch { input.dataFrame(1.ms, sampleRate = 0.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
        }
        it("should fail if sample rate is negative while attempting to create a dataframe of finite stream") {
            assertThat(catch { input.trim(1).dataFrame(sampleRate = -1.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
        }
        it("should fail if sample rate is zero while attempting to create a dataframe of finite stream") {
            assertThat(catch { input.trim(1).dataFrame(sampleRate = 0.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
        }
    }

    describe("Int with custom mapper") {
        val input = input { (i, _) -> if (i < 1000) i.toInt() else null }
        val mapper: (Int) -> String = { "$it".padStart(3, '0') }

        it("should create a dataframe of finite (by interface) stream") {
            assertThat(input.trim(5).dataFrame(sampleRate = 1000.0f, mapper = mapper)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf("000", "001", "002", "003", "004")
            ))
        }
        it("should create a dataframe of infinite (by interface) stream") {
            assertThat(input.dataFrame(5.ms, sampleRate = 1000.0f, mapper = mapper)).isEqualTo(mapOf(
                    COL_TIME to listOf(0.0, 0.001, 0.002, 0.003, 0.004),
                    COL_VALUE to listOf("000", "001", "002", "003", "004")
            ))
        }
    }

    describe("FftSample") {
        val input = 40.sine().window(5).fft(8)

        describe("Sequence") {
            describe("on finite stream") {
                it("should create a data frame with all frequencies") {
                    assertThat(input.trim(15).dataFrame(sampleRate = 1000.0f)).all {
                        time().isEqualTo(listOf(
                                0.000, 0.000, 0.000, 0.000,
                                0.005, 0.005, 0.005, 0.005,
                                0.010, 0.010, 0.010, 0.010,
                        ))
                        frequency().isEqualTo(listOf(
                                0.0, 125.0, 250.0, 375.0,
                                0.0, 125.0, 250.0, 375.0,
                                0.0, 125.0, 250.0, 375.0,
                        ))
                        value().size().isEqualTo(12)
                    }
                }
                it("should create a data frame with limited frequencies") {
                    assertThat(input.trim(15).dataFrame(sampleRate = 1000.0f, freqCutOff = 100 to 300)).all {
                        time().isEqualTo(listOf(
                                0.000, 0.000,
                                0.005, 0.005,
                                0.010, 0.010,
                        ))
                        frequency().isEqualTo(listOf(
                                125.0, 250.0,
                                125.0, 250.0,
                                125.0, 250.0,
                        ))
                        value().size().isEqualTo(6)
                    }
                }
                it("should fail on negative sample rate") {
                    assertThat(catch { input.trim(1).dataFrame(sampleRate = -1.0f) })
                            .isNotNull()
                            .message().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
                }
                it("should fail on zero sample rate") {
                    assertThat(catch { input.trim(1).dataFrame(sampleRate = 0.0f) })
                            .isNotNull()
                            .message().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
                }
                it("should fail on freq cut off negative first argument") {
                    assertThat(catch { input.trim(1).dataFrame(freqCutOff = -1 to 200) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff low boundary must be greater or equal to 0, but -1 found")
                }
                it("should fail on freq cut off if first argument greater than second") {
                    assertThat(catch { input.trim(1).dataFrame(freqCutOff = 201 to 200.0) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff high boundary must be greater than low, but 200.0 found " +
                                    "while the low is 201")
                }
                it("should fail on freq cut off if first argument equal to second") {
                    assertThat(catch { input.trim(1).dataFrame(freqCutOff = 200.0f to 200) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff high boundary must be greater than low, but 200 found " +
                                    "while the low is 200.0")
                }
            }
            describe("on infinite stream") {
                it("should create a data frame with all frequencies") {
                    assertThat(input.dataFrame(15.ms, sampleRate = 1000.0f)).all {
                        time().isEqualTo(listOf(
                                0.000, 0.000, 0.000, 0.000,
                                0.005, 0.005, 0.005, 0.005,
                                0.010, 0.010, 0.010, 0.010,
                        ))
                        frequency().isEqualTo(listOf(
                                0.0, 125.0, 250.0, 375.0,
                                0.0, 125.0, 250.0, 375.0,
                                0.0, 125.0, 250.0, 375.0,
                        ))
                        value().size().isEqualTo(12)
                    }
                }
                it("should create a data frame with limited frequencies") {
                    assertThat(input.dataFrame(15.ms, sampleRate = 1000.0f, freqCutOff = 100 to 300)).all {
                        time().isEqualTo(listOf(
                                0.000, 0.000,
                                0.005, 0.005,
                                0.010, 0.010,
                        ))
                        frequency().isEqualTo(listOf(
                                125.0, 250.0,
                                125.0, 250.0,
                                125.0, 250.0,
                        ))
                        value().size().isEqualTo(6)
                    }
                }
                it("should fail on negative sample rate") {
                    assertThat(catch { input.dataFrame(1.ms, sampleRate = -1.0f) })
                            .isNotNull()
                            .message().isEqualTo("sampleRate must be greater than 0, but -1.0 found")
                }
                it("should fail on zero sample rate") {
                    assertThat(catch { input.dataFrame(1.ms, sampleRate = 0.0f) })
                            .isNotNull()
                            .message().isEqualTo("sampleRate must be greater than 0, but 0.0 found")
                }
                it("should fail on freq cut off negative first argument") {
                    assertThat(catch { input.dataFrame(1.ms, freqCutOff = -1 to 200) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff low boundary must be greater or equal to 0, but -1 found")
                }
                it("should fail on freq cut off if first argument greater than second") {
                    assertThat(catch { input.dataFrame(1.ms, freqCutOff = 201 to 200.0) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff high boundary must be greater than low, but 200.0 found " +
                                    "while the low is 201")
                }
                it("should fail on freq cut off if first argument equal to second") {
                    assertThat(catch { input.dataFrame(1.ms, freqCutOff = 200.0f to 200) })
                            .isNotNull()
                            .message().isEqualTo("freqCutOff high boundary must be greater than low, but 200 found " +
                                    "while the low is 200.0")
                }
            }
        }

        describe("Singular") {
            it("should create a data frame with all frequencies") {
                assertThat(input.dataFrameAt(2.ms, sampleRate = 1000.0f)).all {
                    frequency().isEqualTo(listOf(0.0, 125.0, 250.0, 375.0))
                    value().size().isEqualTo(4)
                }
            }
            it("should create a data frame with limited frequencies") {
                assertThat(input.dataFrameAt(2.ms, sampleRate = 1000.0f, freqCutOff = 100 to 300)).all {
                    frequency().isEqualTo(listOf(125.0, 250.0))
                    value().size().isEqualTo(2)
                }
            }
        }
    }
})

@Suppress("UNCHECKED_CAST")
private fun Assert<WbDataFrame>.time(): Assert<List<Double>> = prop(COL_TIME) { it.getValue(COL_TIME) as List<Double> }

@Suppress("UNCHECKED_CAST")
private fun Assert<WbDataFrame>.frequency(): Assert<List<Double>> = prop(COL_FREQUENCY) { it.getValue(COL_FREQUENCY) as List<Double> }

@Suppress("UNCHECKED_CAST")
private fun Assert<WbDataFrame>.value(): Assert<List<Double>> = prop(COL_VALUE) { it.getValue(COL_VALUE) as List<Double> }
