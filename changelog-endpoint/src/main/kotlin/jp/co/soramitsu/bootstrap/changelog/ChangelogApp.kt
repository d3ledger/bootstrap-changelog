/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JvmName("ChangelogAppMain")

package jp.co.soramitsu.bootstrap.changelog

import com.d3.commons.config.PROFILE_ENV
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import jp.co.soramitsu.bootstrap.changelog.config.changelogConfig
import jp.co.soramitsu.bootstrap.changelog.endpoint.changelogModule
import jp.co.soramitsu.bootstrap.changelog.endpoint.healthCheckModule
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import mu.KLogging
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import kotlin.system.exitProcess

private val logger = KLogging().logger

@ComponentScan
class ChangelogApp

/**
 * Changelog main endpoint
 */
fun main() {
    Result.of {
        val context = AnnotationConfigApplicationContext()
        context.environment.setActiveProfiles(getChangelogProfile())
        context.register(ChangelogApp::class.java)
        context.refresh()
        context
    }.map { context ->
        val changelogExecutor = context.getBean(ChangelogExecutorService::class.java)
        val env = applicationEngineEnvironment {
            module {
                changelogModule(changelogExecutor)
                healthCheckModule()
            }
            connector {
                port = changelogConfig.port
            }
        }
        embeddedServer(Netty, env).start(true)
    }.failure { ex ->
        logger.error("Cannot start changelog service", ex)
        exitProcess(1)
    }
}

/**
 * Returns current profile based on environment variable
 */
fun getChangelogProfile(): String {
    val defaultProfile = "prod"
    var profile = System.getenv(PROFILE_ENV)
    if (profile == null) {
        logger.warn("No profile set for changelog. Using default '$defaultProfile' profile")
        profile = defaultProfile
    } else {
        logger.info("Profile is $profile")
    }
    return profile
}
