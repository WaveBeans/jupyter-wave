package io.wavebeans.jupyter

import io.wavebeans.lib.*
import io.wavebeans.lib.stream.map
import io.wavebeans.lib.stream.trim
import io.wavebeans.lib.stream.window.window
import io.wavebeans.lib.table.TableOutput
import io.wavebeans.lib.table.TableOutputParams
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun BeanStream<Sample>.preview(
        sampleRate: Float = 44100.0f,
        maxLength: TimeMeasure = 10.m
): PreviewSampleBeanStream = PreviewSampleBeanStream(
        this.trim(maxLength.ns(), TimeUnit.NANOSECONDS).window(1024).map { sampleVectorOf(it) },
        PreviewSampleBeanParams(sampleRate, maxLength)
)

class PreviewSampleBeanParams(
        val sampleRate: Float,
        val maxLength: TimeMeasure
) : BeanParams()

class PreviewSampleBeanStream(
        override val input: BeanStream<SampleVector>,
        override val parameters: PreviewSampleBeanParams
) : BeanStream<SampleVector>, SinkBean<SampleVector> {

    override fun asSequence(sampleRate: Float): Sequence<SampleVector> = input.asSequence(sampleRate)

    fun renderPreview(): String {
        val tableName = createPreview()

        val server = "${Config.instance.advertisedProtocol}://${Config.instance.advertisedHost}:${Config.instance.advertisedPort}"
        return """
            <div id="$tableName"></div>
            <script>
                WaveView('$server/audio/$tableName/stream/wav?offset=${parameters.maxLength}', '$tableName').init()
            </script>
        """.trimIndent()
    }

    internal fun createPreview(): String {
        val alphabet = "qazwsxedcrfvtgbyhnujmikolp1234567890QAZWSXEDCRFVTGBYHNUJMIKOLP".toCharArray()
        val tableName = (0..9).map { alphabet[Random.nextInt(alphabet.size)] }.joinToString("")
        val tableOutput = TableOutput(this, parameters = TableOutputParams(
                tableName,
                SampleVector::class,
                parameters.maxLength,
                true
        ))

        Evaluator.evalTableOutput(tableOutput, parameters.sampleRate)
        return tableName
    }
}
