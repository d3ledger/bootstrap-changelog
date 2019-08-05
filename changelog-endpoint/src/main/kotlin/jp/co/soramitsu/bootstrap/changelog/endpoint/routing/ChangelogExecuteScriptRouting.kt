/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.endpoint.routing

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import de.nielsfalk.ktor.swagger.created
import de.nielsfalk.ktor.swagger.description
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogRequestDetails
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogResponse
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogScriptRequest
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import jp.co.soramitsu.bootstrap.changelog.service.ExecutionStatus
import mu.KLogging

private val logger = KLogging().logger

@Group("changelog")
@Location("/changelog/changelogScript")
class ChangelogScript

/**
 * Changelog script based routing
 * @param changelogExecutorService - executor of changelogs
 */
fun Routing.changelogScript(changelogExecutorService: ChangelogExecutorService) {
    post<ChangelogScript, ChangelogScriptRequest>(
        "execute"
            .description("Executes changelog script")
            .responds(created<ChangelogResponse>())
    ) { _, changelogRequest ->
        // Validate and execute changelog
        executeChangelog(changelogRequest, changelogExecutorService).fold(
            { executionStatus ->
                if (executionStatus == ExecutionStatus.SUCCESS) {
                    call.respond(ChangelogResponse.ok())
                } else if (executionStatus == ExecutionStatus.ALREADY_EXECUTED) {
                    call.respond(HttpStatusCode.NotModified)
                }
            },
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
 * @param changelogExecutorService - service that executes changelogs
 */
private fun executeChangelog(
    request: ChangelogScriptRequest,
    changelogExecutorService: ChangelogExecutorService
): Result<ExecutionStatus, Exception> {
    return Result.of {
        //Validate request
        validateChangelogRequest(request.details)
    }.map {
        //Execute request
        changelogExecutorService.execute(request)
    }
}

/**
 * Validates changelog request details
 * @param request - request to check
 * @throws IllegalArgumentException if changelog is invalid
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

