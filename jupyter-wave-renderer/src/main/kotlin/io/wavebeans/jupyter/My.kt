package io.wavebeans.jupyter

import io.wavebeans.lib.*

class PreviewBeanStream<T: Any>(
    override val input: BeanStream<T>,
    override val parameters: NoParams
) : BeanStream<T>, SinkBean<T> {

    override fun asSequence(sampleRate: Float): Sequence<T> = input.asSequence(sampleRate)

    fun createPreview(): String {
      val tableName = "my"
      return """
      <audio controls src="localhost:12345/table/my/stream">Upgrade your browser, bro!</audio>
      """.trimIndent()
    }
}
