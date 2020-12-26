package io.wavebeans.jupyter

import io.wavebeans.execution.SingleThreadedOverseer
import io.wavebeans.fs.dropbox.DropboxWbFileDriver
import io.wavebeans.http.HttpService
import io.wavebeans.lib.table.TableOutput
import mu.KotlinLogging
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

fun getEnvVar(name: String, getenv: (String) -> (String?) = { System.getenv(it) }): String? {
    val v = getenv(name)
    return v?.replace("${'\\'}${'$'}\\{([\\w_]+)\\}".toRegex()) {
        val innerVar = it.groupValues[1]
        getenv(innerVar) ?: throw IllegalStateException("Env var `$innerVar` is not populated")
    }
}

object Evaluator {

    private val log = KotlinLogging.logger { }

    private var httpService: HttpService? = null

    private val executor = Executors.newScheduledThreadPool(1)

    private val tableTrackTasks = ConcurrentHashMap<String, Future<*>>()

    init {
        Config.instance.watch(Config.httpPortVar) {
            val httpPort = it?.toInt()
            if (httpPort != null) {
                log.info { "Initiating http service on port $httpPort. Current instance is $httpService" }
                httpService?.close()
                httpService = HttpService(serverPort = httpPort).start()
            } else {
                log.info { "Stopping http service $httpService" }
                httpService?.close()
            }
        }
        Config.instance.watch(Config.dropboxAccessTokenVar) {
            val clientIdentifier = Config.instance.dropBoxClientIdentifier
            val accessToken = Config.instance.dropBoxAccessToken
            if (!clientIdentifier.isNullOrEmpty() && !accessToken.isNullOrEmpty()) {
                log.info {
                    "Initializing Dropbox File Driver " +
                            "clientIdentifier=****${clientIdentifier.takeLast(6)}, " +
                            "accessToken=****${accessToken.takeLast(6)}"
                }
                DropboxWbFileDriver.configure(
                        clientIdentifier = clientIdentifier,
                        accessToken = accessToken,
                        force = true
                )
            }
        }
    }

    fun initEnvironment() {
        Config.instance.readEnv()
    }

    fun evalTableOutput(output: TableOutput<*>, sampleRate: Float) {

        log.info { "Evaluating output $output with sample rate $sampleRate" }
        val overseer = SingleThreadedOverseer(listOf(output))
        val evalFuture = overseer.eval(sampleRate)

        val tableName = output.parameters.tableName
        val tracker = LocalTableActivityTracker.createAndRegister(tableName)
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
        return PreviewSampleBeanStream.getInitHtml()
    }
}

