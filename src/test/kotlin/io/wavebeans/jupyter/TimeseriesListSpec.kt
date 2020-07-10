package io.wavebeans.jupyter

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.wavebeans.lib.m
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


private val Number.sec: Long
    get() = (60000 * this.toDouble()).toLong()

private val Number.min: Long
    get() = (this.toDouble() * 60).sec

object TimeseriesListSpec : Spek({
    describe("Int timeseries list with default 60 sec granular") {
        val list = TimeseriesList<Int> { a, b -> a + b }

        beforeEachTest { list.reset() }

        it("should return sum of added values inside the range. All values") {
            assertThat (list.append(1, 1.sec)).isTrue()
            assertThat (list.append(2, 2.sec)).isTrue()
            assertThat (list.append(3, 3.sec)).isTrue()
            assertThat(list.inLast(5.m, 4.sec)).isEqualTo(6)
        }
        it("should return sum of added values inside the range. Not all values") {
            assertThat (list.append(1, 1.sec)).isTrue()
            assertThat (list.append(2, 2.sec)).isTrue()
            assertThat (list.append(3, 3.sec)).isTrue()
            assertThat(list.inLast(2.m, 4.sec)).isEqualTo(5)
        }
        it("should clean up some values") {
            assertThat (list.append(1, 1.sec)).isTrue()
            assertThat (list.append(2, 2.sec)).isTrue()
            assertThat (list.append(3, 3.sec)).isTrue()
            list.leaveOnlyLast(2.m, 3.5.sec)
            assertThat(list.inLast(5.m, 4.sec)).isEqualTo(5)
        }
        it("should clean up values in non-committed granular") {
            assertThat (list.append(1, 1.sec)).isTrue()
            assertThat (list.append(2, 2.sec)).isTrue()
            assertThat (list.append(3, 3.sec)).isTrue()
            list.leaveOnlyLast(1.m, 1.5.min)
            assertThat(list.inLast(5.m, 2.min)).isNull()
        }
    }
})