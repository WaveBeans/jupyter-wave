package io.wavebeans.jupyter

import io.wavebeans.execution.SingleThreadedOverseer
import io.wavebeans.execution.metrics.MetricService
import io.wavebeans.http.HttpService
import io.wavebeans.lib.table.TableOutput
import mu.KotlinLogging
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

object Evaluator {

    private val log = KotlinLogging.logger { }

    private var httpService: HttpService? = null

    private val executor = Executors.newScheduledThreadPool(1)

    private val tableTrackTasks = ConcurrentHashMap<String, Future<*>>()

    fun initEnvironment(
            httpPort: Int = 12345
    ) {
        log.info { "Initiating http service on port $httpPort. Current instance is $httpService" }
        if (httpService == null) {
            httpService = HttpService(serverPort = httpPort).start()
        }
    }

    fun evalTableOutput(output: TableOutput<*>, sampleRate: Float) {

        log.info { "Evaluating output $output with sample rate $sampleRate" }
        val overseer = SingleThreadedOverseer(listOf(output))
        val evalFuture = overseer.eval(sampleRate)

        val tableName = output.parameters.tableName
        val tracker = TableActivityTracker(tableName)
        MetricService.registerConnector(tracker)
        tableTrackTasks[tableName]?.cancel(true) // cancel if table track task is already running
        tableTrackTasks[tableName] = executor.scheduleWithFixedDelay({
            try {
                if (!tracker.isStillActive()) {
                    log.info { "Table $tableName is not active anymore" }
                    evalFuture.forEach { if (!it.isDone) it.cancel(true) }
                    overseer.close()
                    tableTrackTasks.remove(tableName)
                            ?.cancel(false)
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    log.error(e) { "Exception working with tracker for table $tableName" }
                }
            }
        }, 0, 5000, MILLISECONDS)
    }

    fun getInitJsHtml(): String {
        return """
            <script type="text/javascript" src="https://unpkg.com/wavesurfer.js"></script>
            <script type="text/javascript">
                ${javaClass.getResourceAsStream("/audio.js").reader().readText()}
            </script>
        """.trimIndent()
    }
}

