package io.wavebeans.jupyter

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object GetEnvVarSpec : Spek({
    describe("With another var") {
        it("should replace inner var") {
            val getenv = { name: String ->
                when (name) {
                    "SOME_VAR" -> "abc_${'$'}{ANOTHER_VAR}_def"
                    "ANOTHER_VAR" -> "zzz"
                    else -> throw UnsupportedOperationException(name)
                }
            }

            assertThat(getEnvVar("SOME_VAR", getenv)).isEqualTo("abc_zzz_def")
        }
    }
})