package io.wavebeans.jupyter

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

private val managementPort = 28440

object ConfigSpec : Spek({
    describe("Management API") {
        val config by memoized(CachingMode.SCOPE) {
            Config {
                when (it) {
                    "MANAGEMENT_SERVER_PORT" -> "$managementPort"
                    "ADVERTISED_HTTP_HOST" -> "some-host"
                    "ADVERTISED_HTTP_PORT" -> "4242"
                    "ADVERTISED_HTTP_PROTOCOL" -> "protocol"
                    "HTTP_PORT" -> "42"
                    "DROPBOX_CLIENT_IDENTIFIER" -> "client-identifier"
                    "DROPBOX_ACCESS_TOKEN" -> "access-token"
                    else -> throw UnsupportedOperationException(it)
                }
            }
        }

        beforeGroup { config.readEnv() }

        afterGroup { config.close() }

        it("should return specified values for ADVERTISED_HTTP_HOST") {
            "ADVERTISED_HTTP_HOST".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("some-host")
            }
        }
        it("should return specified values for ADVERTISED_HTTP_PORT") {
            "ADVERTISED_HTTP_PORT".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("4242")
            }
        }
        it("should return specified values for ADVERTISED_HTTP_PROTOCOL") {
            "ADVERTISED_HTTP_PROTOCOL".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("protocol")
            }
        }
        it("should return specified values for HTTP_PORT") {
            "HTTP_PORT".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("42")
            }
        }
        it("should return specified values for DROPBOX_CLIENT_IDENTIFIER") {
            "DROPBOX_CLIENT_IDENTIFIER".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("client-identifier")
            }
        }
        it("should return specified values for DROPBOX_ACCESS_TOKEN") {
            "DROPBOX_ACCESS_TOKEN".assertGet().all {
                statusCode().isEqualTo(200)
                body().isEqualTo("access-token")
            }
        }
        it("should not return anything for unexisting entry") {
            "UNEXISTING".assertGet().all {
                statusCode().isEqualTo(404)
                body().isEmpty()
            }
        }
        it("should not return anything for empty entry") {
            "".assertGet().all {
                statusCode().isEqualTo(404)
            }
        }

        it("should update ADVERTISED_HTTP_HOST with new value") {
            "ADVERTISED_HTTP_HOST".assertPost("new-host").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.advertisedHost).isEqualTo("new-host")
        }
        it("should update ADVERTISED_HTTP_PORT with new value") {
            "ADVERTISED_HTTP_PORT".assertPost("424242").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.advertisedPort).isEqualTo(424242)
        }
        it("should update ADVERTISED_HTTP_PROTOCOL with new value") {
            "ADVERTISED_HTTP_PROTOCOL".assertPost("secure-protocol").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.advertisedProtocol).isEqualTo("secure-protocol")
        }
        it("should update HTTP_PORT with new value") {
            "HTTP_PORT".assertPost("421").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.httpPort).isEqualTo(421)
        }
        it("should update DROPBOX_CLIENT_IDENTIFIER with new value") {
            "DROPBOX_CLIENT_IDENTIFIER".assertPost("new-identifier").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.dropBoxClientIdentifier).isEqualTo("new-identifier")
        }
        it("should update DROPBOX_ACCESS_TOKEN with new value") {
            "DROPBOX_ACCESS_TOKEN".assertPost("new-token").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.dropBoxAccessToken).isEqualTo("new-token")
        }
        it("should update UNEXISTING with the value") {
            "UNEXISTING".assertPost("the-value").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(config.get("UNEXISTING")).isEqualTo("the-value")
        }
        it("should not update empty") {
            "".assertPost("any-value").all {
                statusCode().isEqualTo(404)
            }
        }
        it("should watch DROPBOX_ACCESS_TOKEN") {
            var watchedValue: String? = null
            config.watch("DROPBOX_ACCESS_TOKEN") { watchedValue = it}
            "DROPBOX_ACCESS_TOKEN".assertPost("new-token").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(watchedValue).isEqualTo("new-token")
        }
        it("should update UNEXISTING with the value") {
            var watchedValue: String? = null
            config.watch("UNEXISTING") { watchedValue = it}
            "UNEXISTING".assertPost("the-value").all {
                statusCode().isEqualTo(200)
                body().isEmpty()
            }
            assertThat(watchedValue).isEqualTo("the-value")
        }
    }

    describe("Regular API") {
        val config by memoized(CachingMode.EACH_GROUP) {
            Config {
                when (it) {
                    "MANAGEMENT_SERVER_PORT" -> null
                    "ADVERTISED_HTTP_HOST" -> "some-host"
                    "ADVERTISED_HTTP_PORT" -> "4242"
                    "ADVERTISED_HTTP_PROTOCOL" -> "protocol"
                    "HTTP_PORT" -> "42"
                    "DROPBOX_CLIENT_IDENTIFIER" -> "client-identifier"
                    "DROPBOX_ACCESS_TOKEN" -> "access-token"
                    else -> throw UnsupportedOperationException(it)
                }
            }
        }

        beforeEachGroup { config.readEnv() }

        describe("Reading values") {
            it("should return value forADVERTISED_HTTP_HOST") { assertThat(config.advertisedHost).isEqualTo("some-host") }
            it("should return value for ADVERTISED_HTTP_PORT") { assertThat(config.advertisedPort).isEqualTo(4242) }
            it("should return value for ADVERTISED_HTTP_PROTOCOL") { assertThat(config.advertisedProtocol).isEqualTo("protocol") }
            it("should return value for HTTP_PORT") { assertThat(config.httpPort).isEqualTo(42) }
            it("should return value for DROPBOX_CLIENT_IDENTIFIER") { assertThat(config.dropBoxClientIdentifier).isEqualTo("client-identifier") }
            it("should return value for DROPBOX_ACCESS_TOKEN") { assertThat(config.dropBoxAccessToken).isEqualTo("access-token") }
            it("should return nothing for UNEXISTING") { assertThat(config.get("UNEXISTING")).isNull() }
        }

        describe("Writing values") {
            it("should set new value for ADVERTISED_HTTP_HOST") {
                config.set("ADVERTISED_HTTP_HOST", "new-host")
                assertThat(config.advertisedHost).isEqualTo("new-host")
            }
            it("should set new value for  ADVERTISED_HTTP_PORT") {
                config.set("ADVERTISED_HTTP_PORT", "424242")
                assertThat(config.advertisedPort).isEqualTo(424242)
            }
            it("should set new value for  ADVERTISED_HTTP_PROTOCOL") {
                config.set("ADVERTISED_HTTP_PROTOCOL", "secure-protocol")
                assertThat(config.advertisedProtocol).isEqualTo("secure-protocol")
            }
            it("should set new value for  HTTP_PORT") {
                config.set("HTTP_PORT", "421")
                assertThat(config.httpPort).isEqualTo(421)
            }
            it("should set new value for  DROPBOX_CLIENT_IDENTIFIER") {
                config.set("DROPBOX_CLIENT_IDENTIFIER", "new-identifier")
                assertThat(config.dropBoxClientIdentifier).isEqualTo("new-identifier")
            }
            it("should set new value for DROPBOX_ACCESS_TOKEN") {
                config.set("DROPBOX_ACCESS_TOKEN", "new-token")
                assertThat(config.dropBoxAccessToken).isEqualTo("new-token")
            }
            it("should set value for UNEXISTING") {
                config.set("UNEXISTING", "the-value")
                assertThat(config.get("UNEXISTING")).isEqualTo("the-value")
            }
            it("should set null value for UNEXISTING") {
                config.set("UNEXISTING", null)
                assertThat(config.get("UNEXISTING")).isNull()
            }
        }
        describe("Watching values") {
            it("should watch ADVERTISED_HTTP_HOST") {
                var watchedValue: String? = null
                config.watch("ADVERTISED_HTTP_HOST") { watchedValue = it}
                config.set("ADVERTISED_HTTP_HOST", "new-host")
                assertThat(watchedValue).isEqualTo("new-host")
            }
            it("should watch UNEXISTING") {
                var watchedValue: String? = null
                config.watch("UNEXISTING") { watchedValue = it}
                config.set("UNEXISTING", "the-value")
                assertThat(watchedValue).isEqualTo("the-value")
            }
        }
    }
})

private fun Assert<HttpResponse<String>>.body() = prop("body") { it.body }

private fun Assert<HttpResponse<String>>.statusCode() = prop("statusCode") { it.status }

private fun String.assertGet() = assertThat(Unirest.get("http://localhost:$managementPort/config/$this")).prop("asString") { it.asString() }

private fun String.assertPost(newValue: String) =
        assertThat(
                Unirest.post("http://localhost:$managementPort/config/$this")
                        .body(newValue)
        ).prop("asString") { it.asString() }
