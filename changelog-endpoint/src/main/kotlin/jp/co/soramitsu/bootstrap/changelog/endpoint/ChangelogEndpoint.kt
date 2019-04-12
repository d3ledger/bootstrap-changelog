package jp.co.soramitsu.bootstrap.changelog.endpoint

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import de.nielsfalk.ktor.swagger.SwaggerSupport
import de.nielsfalk.ktor.swagger.version.v2.Swagger
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.locations.Locations
import io.ktor.routing.routing
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogRequestDetails
import jp.co.soramitsu.bootstrap.changelog.endpoint.routing.changelogFile
import jp.co.soramitsu.bootstrap.changelog.endpoint.routing.changelogScript
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService


/**
 * Main changelog ktor module
 * @param changelogExecutorService - executor of changelogs
 */
fun Application.changelogModule(changelogExecutorService: ChangelogExecutorService) {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Locations)
    install(SwaggerSupport) {
        swagger = Swagger()
    }
    routing {
        //Changelog script based endpoint
        changelogScript(changelogExecutorService)
        //Changelog file based endpoint
        changelogFile(changelogExecutorService)
    }
}

/**
 * Executes changelog
 * @param request - changelog request
 * @param executor - changelog execution logic
 */
fun validateChangelog(
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
fun validateChangelogRequest(request: ChangelogRequestDetails) {
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
