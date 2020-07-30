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

object Config {

    var advertisedHost: String = "localhost"
    var advertisedPort: Int? = null
    var advertisedProtocol: String = "http"
    var httpPort: Int? = null
    var dropBoxClientIdentifier: String? = null
    var dropBoxAccessToken: String? = null

    fun readEnv() {
        advertisedHost = getEnvVar("ADVERTISED_HTTP_HOST") ?: "localhost"
        advertisedPort = getEnvVar("ADVERTISED_HTTP_PORT")?.toInt()
        advertisedProtocol = getEnvVar("ADVERTISED_HTTP_PROTOCOL") ?: "http"
        httpPort = getEnvVar("HTTP_PORT")?.toInt()
        dropBoxClientIdentifier = getEnvVar("DROPBOX_CLIENT_IDENTIFIER")
        dropBoxAccessToken = getEnvVar("DROPBOX_ACCESS_TOKEN")
    }

}

object Evaluator {

    private val log = KotlinLogging.logger { }

    private var httpService: HttpService? = null

    private val executor = Executors.newScheduledThreadPool(1)

    private val tableTrackTasks = ConcurrentHashMap<String, Future<*>>()

    fun initEnvironment() {
        Config.readEnv()
        init()
    }

    fun init(restartHttp: Boolean = false) {
        val httpPort = Config.httpPort
        if (httpPort != null) {
            log.info { "Initiating http service on port $httpPort. Current instance is $httpService" }
            if (httpService == null) {
                httpService = HttpService(serverPort = httpPort).start()
            } else if (restartHttp) {
                httpService?.close()
                httpService = HttpService(serverPort = httpPort).start()
            }
        }

        val clientIdentifier = Config.dropBoxClientIdentifier
        val accessToken = Config.dropBoxAccessToken
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
        return """
            <script type="text/javascript" src="https://unpkg.com/wavesurfer.js"></script>
            <script type="text/javascript">
                ${javaClass.getResourceAsStream("/audio.js").reader().readText()}
            </script>
        """.trimIndent()
    }
}

