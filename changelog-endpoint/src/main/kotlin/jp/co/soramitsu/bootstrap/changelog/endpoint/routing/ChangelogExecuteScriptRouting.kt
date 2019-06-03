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
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogResponse
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogScriptRequest
import jp.co.soramitsu.bootstrap.changelog.endpoint.validateChangelog
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
