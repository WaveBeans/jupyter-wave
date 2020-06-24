package io.wavebeans.jupyter

import io.wavebeans.execution.SingleThreadedOverseer
import io.wavebeans.http.HttpService
import io.wavebeans.lib.io.StreamOutput

var httpService: HttpService? = null

fun initEnvironment(
        httpPort: Int = 12345
) {
    httpService?.close()

    httpService = HttpService(serverPort = httpPort).start()
}

fun evalOutput(output: StreamOutput<*>, sampleRate: Float) {
    val overseer = SingleThreadedOverseer(listOf(output))
    overseer.eval(sampleRate)
}