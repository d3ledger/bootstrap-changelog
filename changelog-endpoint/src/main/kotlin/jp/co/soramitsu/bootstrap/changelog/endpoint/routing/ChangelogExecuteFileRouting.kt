/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.endpoint.routing

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
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogFileRequest
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogResponse
import jp.co.soramitsu.bootstrap.changelog.endpoint.validateChangelog
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import jp.co.soramitsu.bootstrap.changelog.service.ExecutionStatus
import mu.KLogging

private val logger = KLogging().logger

@Group("changelog")
@Location("/changelog/changelogFile")
class ChangelogFile

/**
 * Changelog file based routing
 * @param changelogExecutorService - executor of changelogs
 */
fun Routing.changelogFile(changelogExecutorService: ChangelogExecutorService) {
    post<ChangelogFile, ChangelogFileRequest>(
        "execute"
            .description("Executes changelog file")
            .responds(created<ChangelogResponse>())
    ) { _, changelogRequest ->
        // Validate and execute changelog
        validateChangelog(changelogRequest.details)
        { changelogExecutorService.execute(changelogRequest) }.fold(
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
