package io.wavebeans.jupyter

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.message
import assertk.catch
import io.wavebeans.lib.EmptySampleVector
import io.wavebeans.lib.sampleVectorOf
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SampleVectorExtSpec : Spek({
    describe("Data frame extension") {
        it("should create a data frame with default values indexShift=0, indexStep=0, sampleRate=null") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame()).isEqualTo(mapOf(
                    "index" to listOf(0, 1, 2, 3),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04)

            ))
        }
        it("should create a data frame with indexShift=1") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexShift = 1)).isEqualTo(mapOf(
                    "index" to listOf(1, 2, 3, 4),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04)

            ))
        }
        it("should create a data frame with indexStep=2") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexStep = 2)).isEqualTo(mapOf(
                    "index" to listOf(0, 2, 4, 6),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04)

            ))
        }
        it("should create a data frame with indexShift=3 and indexStep=2") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexShift = 3, indexStep = 2)).isEqualTo(mapOf(
                    "index" to listOf(3, 5, 7, 9),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04)
            ))
        }
        it("should create a data frame with time column if sampleRate=1000.0") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(sampleRate = 1000.0f)).isEqualTo(mapOf(
                    "index" to listOf(0, 1, 2, 3),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04),
                    "time" to listOf(0.0, 0.001, 0.002, 0.003),
            ))
        }
        it("should create a data frame with time column if sampleRate=1000.0 with indexShift=3 and indexStep=2") {
            assertThat(sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(
                    indexShift = 3,
                    indexStep = 2,
                    sampleRate = 1000.0f
            )).isEqualTo(mapOf(
                    "index" to listOf(3, 5, 7, 9),
                    "value" to listOf(0.01, 0.02, 0.03, 0.04),
                    "time" to listOf(0.003, 0.005, 0.007, 0.009),
            ))
        }
        it("should create empty data frame on empty sample vector") {
            assertThat(EmptySampleVector.dataFrame()).isEqualTo(emptyMap<String, List<Any>>())
        }
        it("should fail with negative shift as a parameter") {
            assertThat(catch { sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexShift = -1) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("indexShift must be greater or equal to 0, but -1 found")
        }
        it("should fail with negative step as a parameter") {
            assertThat(catch { sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexStep = -1) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("indexStep must be greater than 0, but -1 found")
        }
        it("should fail with zero step as a parameter") {
            assertThat(catch { sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(indexStep = 0) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("indexStep must be greater than 0, but 0 found")
        }
        it("should fail with negative sample rate") {
            assertThat(catch { sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(sampleRate = -1.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0.0, but -1.0 found")
        }
        it("should fail with zero sample rate") {
            assertThat(catch { sampleVectorOf(0.01, 0.02, 0.03, 0.04).dataFrame(sampleRate = 0.0f) })
                    .isNotNull()
                    .message().isNotNull().isEqualTo("sampleRate must be greater than 0.0, but 0.0 found")
        }
    }
})