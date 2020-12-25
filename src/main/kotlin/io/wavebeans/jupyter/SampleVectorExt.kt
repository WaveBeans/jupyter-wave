package io.wavebeans.jupyter

import io.wavebeans.lib.SampleVector

fun SampleVector.dataFrame(
        indexShift: Int = 0,
        indexStep: Int = 1,
        sampleRate: Float? = null,
): WbDataFrame {
    require(indexShift >= 0) { "indexShift must be greater or equal to 0, but $indexShift found" }
    require(indexStep > 0) { "indexStep must be greater than 0, but $indexStep found" }
    require(sampleRate == null || sampleRate > 0.0f) { "sampleRate must be greater than 0.0, but $sampleRate found" }
    if (this.isEmpty()) return emptyMap()
    return mapOf(
            COL_INDEX to this.indices.map { it * indexStep + indexShift }.toList(),
            COL_VALUE to this.toList()
    ) + (sampleRate
            ?.let { mapOf(COL_TIME to this.indices.map { (it * indexStep + indexShift) / sampleRate.toDouble() }.toList()) }
            ?: emptyMap<String, List<Any>>()
            )
}