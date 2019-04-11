@file:JvmName("ChangelogAppMain")
package jp.co.soramitsu.bootstrap.changelog
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import jp.co.soramitsu.bootstrap.changelog.endpoint.changelogModule
import jp.co.soramitsu.bootstrap.changelog.endpoint.healthCheckModule
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import mu.KLogging
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan

private val logger = KLogging().logger

@ComponentScan
class ChangelogApp

/**
 * Changelog main endpoint
 */
fun main(args: Array<String>) {
    Result.of {
        AnnotationConfigApplicationContext(ChangelogApp::class.java)
    }.map { context ->
        val changelogExecutor = context.getBean(ChangelogExecutorService::class.java)
        val env = applicationEngineEnvironment {
            module {
                changelogModule(changelogExecutor)
                healthCheckModule()
            }
            connector {
                port = 9999
            }
        }
        embeddedServer(Netty, env).start(true)
    }.failure { ex ->
        logger.error("Cannot start changelog service", ex)
        System.exit(1)
    }
}
