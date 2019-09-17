/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

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
import jp.co.soramitsu.bootstrap.changelog.endpoint.routing.changelogScript
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import jp.co.soramitsu.bootstrap.changelog.service.ExecutionStatus


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
    }
}