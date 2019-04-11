package jp.co.soramitsu.bootstrap.changelog.endpoint

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.routing
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogFileRequest
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogRequestDetails
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogResponse
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogScriptRequest
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import mu.KLogging

private val logger = KLogging().logger

/**
 * Main changelog ktor module
 * @param changelogExecutorService - executor of changelogs
 */
fun Application.changelogModule(changelogExecutorService: ChangelogExecutorService) {
    install(ContentNegotiation) {
        //Install jackson for JSON parsing routines
        jackson {
        }
    }
    routing {
        //Changelog script based endpoint
        changelogScript(changelogExecutorService)
        //Changelog file based endpoint
        changelogFile(changelogExecutorService)
    }
}

/**
 * Changelog file based routing
 * @param changelogExecutorService - executor of changelogs
 */
private fun Routing.changelogFile(changelogExecutorService: ChangelogExecutorService) {
    post("/changelog/changelogFile") {
        val changelogRequest = call.receive<ChangelogFileRequest>()
        // Validate and execute changelog
        validateChangelog(changelogRequest.details)
        { changelogExecutorService.execute(changelogRequest) }.fold(
            { call.respond(ChangelogResponse.ok()) },
            { ex ->
                // Handle errors
                logger.error("Cannot execute changelog", ex)
                call.respond(
                    message = ChangelogResponse.exception(ex),
                    status = HttpStatusCode.Conflict
                )
            })
    }
}

/**
 * Changelog script based routing
 * @param changelogExecutorService - executor of changelogs
 */
private fun Routing.changelogScript(changelogExecutorService: ChangelogExecutorService) {
    post("/changelog/changelogScript") {
        val changelogRequest = call.receive<ChangelogScriptRequest>()
        // Validate and execute changelog
        validateChangelog(changelogRequest.details)
        { changelogExecutorService.execute(changelogRequest) }.fold(
            { call.respond(ChangelogResponse.ok()) },
            { ex ->
                // Handle errors
                logger.error("Cannot execute changelog", ex)
                call.respond(
                    message = ChangelogResponse.exception(ex),
                    status = HttpStatusCode.Conflict
                )
            })
    }
}

/**
 * Executes changelog
 * @param request - changelog request
 * @param executor - changelog execution logic
 */
private fun validateChangelog(
    request: ChangelogRequestDetails,
    executor: () -> Unit
): Result<Unit, Exception> {
    return Result.of {
        //Validate request
        validateChangelogRequest(request)
    }.map { executor() }
}

/**
 * Validates changelog request details
 * @param request - request to check
 */
private fun validateChangelogRequest(request: ChangelogRequestDetails) {
    val emptyPeers = request.peers.filter { peer -> peer.peerKey.isEmpty() }
    if (emptyPeers.isNotEmpty()) {
        val message = "Peers with empty publicKeys: ${emptyPeers.map { emptyPeer -> emptyPeer.hostPort }}"
        throw IllegalArgumentException(message)
    }
    val emptyAccounts = request.accounts.filter { account -> account.pubKeys.any { key -> key.isEmpty() } }
    if (emptyAccounts.isNotEmpty()) {
        val message = "Accounts with empty publicKeys: ${emptyAccounts.map { it.accountName }}"
        throw IllegalArgumentException(message)
    } else if (request.superuserKeys.isEmpty()) {
        val message = "Empty superuser keys"
        throw IllegalArgumentException(message)
    }
}
