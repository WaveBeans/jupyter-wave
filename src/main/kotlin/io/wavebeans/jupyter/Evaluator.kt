package io.wavebeans.jupyter

import io.wavebeans.execution.SingleThreadedOverseer
import io.wavebeans.http.HttpService
import io.wavebeans.lib.table.TableOutput
import mu.KotlinLogging

object Evaluator {

    private val log = KotlinLogging.logger { }

    private var httpService: HttpService? = null

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
        overseer.eval(sampleRate)
    }
}
