package io.wavebeans.jupyter

import io.javalin.Javalin
import mu.KotlinLogging
import org.eclipse.jetty.http.HttpStatus
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Config(
        private val getenv: (String) -> String?
) : Closeable {

    companion object {
        val instance: Config = Config { System.getenv(it) }
        val advertisedHostVar = "ADVERTISED_HTTP_HOST"
        val advertisedPortVar = "ADVERTISED_HTTP_PORT"
        val advertisedProtocolVar = "ADVERTISED_HTTP_PROTOCOL"
        val httpPortVar = "HTTP_PORT"
        val dropBoxClientIdentifierVar = "DROPBOX_CLIENT_IDENTIFIER"
        val dropboxAccessTokenVar = "DROPBOX_ACCESS_TOKEN"
        private val log = KotlinLogging.logger { }
    }

    val advertisedHost: String
        get() = config[advertisedHostVar] ?: "localhost"

    val advertisedPort: Int?
        get() = config[advertisedPortVar]?.toInt() ?: httpPort

    val advertisedProtocol: String
        get() = config[advertisedProtocolVar] ?: "http"

    val httpPort: Int?
        get() = config[httpPortVar]?.toInt()

    val dropBoxClientIdentifier: String?
        get() = config[dropBoxClientIdentifierVar]

    val dropBoxAccessToken: String?
        get() = config[dropboxAccessTokenVar]

    private val config = ConcurrentHashMap<String, String?>()
    private val watchers = ConcurrentHashMap<String, MutableList<(String?) -> Unit>>()
    private val managementServer = getEnvVar("MANAGEMENT_SERVER_PORT", getenv)
            ?.toInt()
            ?.let { Javalin.create().start(it) }

    init {
        Runtime.getRuntime().addShutdownHook(Thread { close() })

        managementServer?.post("/config/:name") { ctx ->
            val name = ctx.pathParam("name")
            set(name, ctx.body())
            ctx.status(HttpStatus.OK_200)
        }

        managementServer?.get("/config/:name") { ctx ->
            val name = ctx.pathParam("name")
            val value = get(name)
            if (value != null) {
                ctx.result(value)
            } else {
                ctx.status(HttpStatus.NOT_FOUND_404)
            }
        }
    }


    fun get(name: String): String? = config[name]

    fun set(name: String, value: String?) {
        try {
            if (value != null) {
                config[name] = value
            } else {
                config.remove(name)
            }
            watchers[name]?.forEach { watcher ->
                try {
                    watcher(value)
                } catch (e: Exception) {
                    log.error(e) { "Error with watcher `$watcher` handling value `$value`" }
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Can't set value `$value` for variable `$name`", e)
        }
    }

    fun watch(name: String, watcher: (String?) -> Unit) {
        watchers.computeIfAbsent(name) { Collections.synchronizedList(ArrayList()) }
        watchers.getValue(name).add(watcher)
    }

    fun readEnv() {
        set(advertisedHostVar, getEnvVar(advertisedHostVar, getenv))
        set(advertisedPortVar, getEnvVar(advertisedPortVar, getenv))
        set(advertisedProtocolVar, getEnvVar(advertisedProtocolVar, getenv))
        set(httpPortVar, getEnvVar(httpPortVar, getenv))
        set(dropBoxClientIdentifierVar, getEnvVar(dropBoxClientIdentifierVar, getenv))
        set(dropboxAccessTokenVar, getEnvVar(dropboxAccessTokenVar, getenv))
    }

    override fun close() {
        managementServer?.stop()
    }
}