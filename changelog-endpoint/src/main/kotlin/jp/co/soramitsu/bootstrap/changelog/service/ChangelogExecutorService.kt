package jp.co.soramitsu.bootstrap.changelog.service

import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogFileRequest
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogRequestDetails
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogScriptRequest
import jp.co.soramitsu.bootstrap.changelog.helper.*
import jp.co.soramitsu.bootstrap.changelog.iroha.sendBatchMST
import jp.co.soramitsu.bootstrap.changelog.parser.ChangelogParser
import jp.co.soramitsu.iroha.java.IrohaAPI
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

/**
 * Service that executes changelog script
 */
@Component
class ChangelogExecutorService(
    @Autowired private val changelogParser: ChangelogParser,
    @Autowired private val changelogHistoryService: ChangelogHistoryService,
    @Autowired private val irohaAPI: IrohaAPI
) {

    private val logger = KLogging().logger

    /**
     * Executes file based changelog
     * @param changelogRequest - request of changelog to execute
     * @return status of execution
     */
    fun execute(changelogRequest: ChangelogFileRequest): ExecutionStatus =
        execute(changelogRequest.details, File(changelogRequest.changelogFile).readText())

    /**
     * Executes script based changelog
     * @param changelogRequest - request of changelog to execute
     * @return status of execution
     */
    fun execute(changelogRequest: ChangelogScriptRequest): ExecutionStatus =
        execute(changelogRequest.details, changelogRequest.script)

    /**
     * Executes changelog
     * @param changelogRequestDetails - changelog request details(environments, project, keys and etc)
     * @param script - script to execute
     * @return status of execution
     */
    @Synchronized
    private fun execute(changelogRequestDetails: ChangelogRequestDetails, script: String): ExecutionStatus {
        // Parse changelog script
        val changelog = changelogParser.parse(script)
        logger.info("Script has been successfully parsed. Script content\n$script")
        if (alreadyExecutedSchema(changelog.schemaVersion, irohaAPI, changelogRequestDetails.superuserKeys)) {
            logger.warn("Schema version '${changelog.schemaVersion}' has been executed already")
            return ExecutionStatus.ALREADY_EXECUTED
        }
        val superuserQuorum = getSuperuserQuorum(irohaAPI, changelogRequestDetails.superuserKeys)
        // Create changelog tx
        val changelogTx = addTxSuperuserQuorum(
            createChangelogTx(changelog, changelogRequestDetails),
            superuserQuorum
        )
        // Create changelog history tx
        val changelogHistoryTx = addTxSuperuserQuorum(
            changelogHistoryService.createHistoryTx(
                changelog.schemaVersion,
                changelogTx.reducedHashHex
            ), superuserQuorum
        )
        // Create changelog batch
        val changelogBatch = createChangelogBatch(changelogTx, changelogHistoryTx)
        // Sign changelog batch
        signChangelogBatch(changelogBatch, changelogRequestDetails.superuserKeys)
        // Send batch
        irohaAPI
            .sendBatchMST(changelogBatch.map { tx -> tx.build() }).fold(
                {
                    logger.info(
                        "Changelog batch (schemaVersion:${changelog.schemaVersion}) has been successfully sent"
                    )
                },
                { ex -> throw ex })
        return ExecutionStatus.SUCCESS
    }
}

// Status of changelog execution
enum class ExecutionStatus {
    SUCCESS,
    ALREADY_EXECUTED
}
