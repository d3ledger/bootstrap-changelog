package jp.co.soramitsu.bootstrap.changelog.service

import jp.co.soramitsu.bootstrap.changelog.ChangelogInterface
import jp.co.soramitsu.iroha.java.Transaction
import mu.KLogging
import org.springframework.stereotype.Component

//Account that stores history in details
const val changelogHistoryStorageAccountId = "changelog_history@bootstrap"

/**
 * Service that is used to create changelog history transactions
 */
@Component
class ChangelogHistoryService {

    private val logger = KLogging().logger

    /**
     * Create history transaction
     * @param schemaVersion - version of changelog schema
     * @param changelogReducedHash - reduced hash of changelog tx
     * @return history transaction
     */
    fun createHistoryTx(schemaVersion: String, changelogReducedHash: String): Transaction {
        logger.info("Changelog history tx (schemaVersion:$schemaVersion,reducedHash:$changelogReducedHash)")
        return Transaction.builder(ChangelogInterface.superuserAccountId).setAccountDetail(
            changelogHistoryStorageAccountId,
            schemaVersion,
            changelogReducedHash
        ).build()
    }

}
