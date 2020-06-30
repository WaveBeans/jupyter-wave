package io.wavebeans.jupyter

import io.wavebeans.lib.*
import io.wavebeans.lib.table.TableOutput
import io.wavebeans.lib.table.TableOutputParams
import kotlin.random.Random
import kotlin.reflect.KClass

inline fun <reified T : Any> BeanStream<T>.preview(
        sampleRate: Float = 44100.0f,
        tableName: String? = null,
        maxLength: TimeMeasure = 10.m
): PreviewBeanStream<T> = PreviewBeanStream(this, PreviewBeanParams(sampleRate, tableName, T::class, maxLength))

class PreviewBeanParams<T : Any>(
        val sampleRate: Float,
        val tableName: String?,
        val clazz: KClass<out T>,
        val maxLength: TimeMeasure
) : BeanParams()

class PreviewBeanStream<T : Any>(
        override val input: BeanStream<T>,
        override val parameters: PreviewBeanParams<T>
) : BeanStream<T>, SinkBean<T> {

    override fun asSequence(sampleRate: Float): Sequence<T> = input.asSequence(sampleRate)

    fun createPreview(): String {
        val alphabet = "qazwsxedcrfvtgbyhnujmikolp1234567890QAZWSXEDCRFVTGBYHNUJMIKOLP".toCharArray()
        val tableName = parameters.tableName ?: (0..9).map { alphabet[Random.nextInt(alphabet.size)] }.joinToString("")
        val tableOutput = TableOutput(this, parameters = TableOutputParams(
                tableName,
                parameters.clazz,
                parameters.maxLength
        ))

        Evaluator.evalOutput(tableOutput, parameters.sampleRate)

        return """
            <audio controls preload="auto" src="http://localhost:12345/audio/$tableName/stream/wav?offset=${parameters.maxLength}">Upgrade your browser, bro!</audio>
        """.trimIndent()
    }
}
