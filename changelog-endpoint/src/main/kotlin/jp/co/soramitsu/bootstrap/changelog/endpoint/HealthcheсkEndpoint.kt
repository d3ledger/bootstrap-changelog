package jp.co.soramitsu.bootstrap.changelog.endpoint

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing

/**
 * Health check ktor module
 */
fun Application.healthCheckModule() {
    routing {
        get("health") {
            call.respondText("Ok")
        }
    }
}
