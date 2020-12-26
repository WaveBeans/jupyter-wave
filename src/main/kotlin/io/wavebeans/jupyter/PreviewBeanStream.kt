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

    companion object {
        fun getInitHtml(): String {
            return """
            <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
            <script type="text/javascript">
                ${this::class.java.getResourceAsStream("/wavesurfer.js").reader().readText()}
                ${this::class.java.getResourceAsStream("/wavesurfer.timeline.js").reader().readText()}
                ${this::class.java.getResourceAsStream("/wavesurfer.cursor.js").reader().readText()}
                ${this::class.java.getResourceAsStream("/wavesurfer.minimap.js").reader().readText()}
                ${this::class.java.getResourceAsStream("/wavesurfer.regions.js").reader().readText()}
            </script>
            <script type="text/javascript">
                ${this::class.java.getResourceAsStream("/audio.js").reader().readText()}
            </script>
        """.trimIndent()
        }
    }


    override fun asSequence(sampleRate: Float): Sequence<SampleVector> = input.asSequence(sampleRate)

    fun renderPreview(): String {
        val tableName = createPreview()

        val server = "${Config.instance.advertisedProtocol}://${Config.instance.advertisedHost}:${Config.instance.advertisedPort}"
        return """
            <div class="$tableName">
                <div class="wave" style="position: relative; overflow: hidden"></div>
                <div class="timeline"></div>
            
                <div class="controls">
                    <button data-action="play"><span class="material-icons">play_arrow</span></button>
                    <button data-action="pause"><span class="material-icons">pause</span></button>
                    <input id="aa" data-action="zoom" type="range" style="width: 60%"/>
                    <div class="regions"></div>
                </div>
            </div>
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

    override val desiredSampleRate: Float? = null
}
