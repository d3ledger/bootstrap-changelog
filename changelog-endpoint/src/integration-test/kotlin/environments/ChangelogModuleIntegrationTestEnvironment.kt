/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package environments

import com.google.gson.Gson
import iroha.protocol.BlockOuterClass
import iroha.protocol.Primitive
import iroha.protocol.QryResponses
import jp.co.soramitsu.bootstrap.changelog.ChangelogAccountPublicInfo
import jp.co.soramitsu.bootstrap.changelog.ChangelogInterface
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogRequestDetails
import jp.co.soramitsu.bootstrap.changelog.dto.ChangelogScriptRequest
import jp.co.soramitsu.bootstrap.changelog.dto.HexKeyPair
import jp.co.soramitsu.bootstrap.changelog.iroha.IrohaBatchConsumerDebug
import jp.co.soramitsu.bootstrap.changelog.parser.ChangelogParser
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogExecutorService
import jp.co.soramitsu.bootstrap.changelog.service.ChangelogHistoryService
import jp.co.soramitsu.bootstrap.changelog.service.changelogHistoryStorageAccountId
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.Query
import jp.co.soramitsu.iroha.java.QueryAPI
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.java.Utils
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import jp.co.soramitsu.iroha.testcontainers.PeerConfig
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import java.io.Closeable
import java.io.File


//Here lies the sample changelog script
private const val SAMPLE_CHANGELOG_PATH = "samples/sample_changelog.groovy"
private const val CLIENT_DOMAIN_ID = "d3"
private const val DEFAULT_QUORUM = 1

/**
 * Environment for changelog ktor module test
 */
class ChangelogModuleIntegrationTestEnvironment : Closeable {

    private val peerKeyPair = Ed25519Sha3().generateKeypair()
    private val notaryKeyPair = Ed25519Sha3().generateKeypair()
    val notaryAccountId = "notary@notary"
    private val changelogHistoryKeyPair = Ed25519Sha3().generateKeypair()
    val superuserKeyPair = Ed25519Sha3().generateKeypair()
    val irohaContainer = IrohaContainer().withPeerConfig(getPeerConfig())

    init {
        irohaContainer.start()
    }

    private val irohaAPI = irohaContainer.api

    /**
     * Returns Iroha peer config
     */
    private fun getPeerConfig(): PeerConfig {
        val config = PeerConfig.builder()
            .genesisBlock(getGenesisBlock())
            .build()

        config.withPeerKeyPair(peerKeyPair)
        return config
    }

    /**
     * Creates changelog genesis block
     */
    private fun getGenesisBlock(): BlockOuterClass.Block {
        return GenesisBlockBuilder().addTransaction(
            Transaction.builder(ChangelogInterface.superuserAccountId)
                .addPeer("0.0.0.0:10001", peerKeyPair.public)
                .createRole("none", emptyList())
                .createDomain("d3", "none")
                .createDomain("notary", "none")
                .createDomain("bootstrap", "none")
                .createRole(
                    "notary",
                    listOf(
                        Primitive.RolePermission.can_grant_can_add_my_signatory,
                        Primitive.RolePermission.can_grant_can_set_my_quorum
                    )
                )
                .createRole(
                    "superuser",
                    listOf(
                        Primitive.RolePermission.can_create_account,
                        Primitive.RolePermission.can_set_detail,
                        Primitive.RolePermission.can_create_asset,
                        Primitive.RolePermission.can_receive,
                        Primitive.RolePermission.can_transfer,
                        Primitive.RolePermission.can_add_asset_qty,
                        Primitive.RolePermission.can_subtract_asset_qty,
                        Primitive.RolePermission.can_add_domain_asset_qty,
                        Primitive.RolePermission.can_subtract_domain_asset_qty,
                        Primitive.RolePermission.can_create_domain,
                        Primitive.RolePermission.can_grant_can_add_my_signatory,
                        Primitive.RolePermission.can_grant_can_remove_my_signatory,
                        Primitive.RolePermission.can_grant_can_set_my_account_detail,
                        Primitive.RolePermission.can_grant_can_set_my_quorum,
                        Primitive.RolePermission.can_grant_can_transfer_my_assets,
                        Primitive.RolePermission.can_add_peer,
                        Primitive.RolePermission.can_append_role,
                        Primitive.RolePermission.can_create_role,
                        Primitive.RolePermission.can_detach_role,
                        Primitive.RolePermission.can_add_signatory,
                        Primitive.RolePermission.can_remove_signatory,
                        Primitive.RolePermission.can_set_quorum,
                        Primitive.RolePermission.can_get_all_acc_detail,
                        Primitive.RolePermission.can_get_all_accounts,
                        Primitive.RolePermission.can_get_domain_acc_detail,
                        Primitive.RolePermission.can_get_domain_accounts,
                        Primitive.RolePermission.can_get_my_acc_detail,
                        Primitive.RolePermission.can_get_my_account,
                        Primitive.RolePermission.can_get_all_acc_ast,
                        Primitive.RolePermission.can_get_domain_acc_ast,
                        Primitive.RolePermission.can_get_my_acc_ast,
                        Primitive.RolePermission.can_get_all_acc_ast_txs,
                        Primitive.RolePermission.can_get_domain_acc_ast_txs,
                        Primitive.RolePermission.can_get_my_acc_ast_txs,
                        Primitive.RolePermission.can_get_all_acc_txs,
                        Primitive.RolePermission.can_get_domain_acc_txs,
                        Primitive.RolePermission.can_get_my_acc_txs,
                        Primitive.RolePermission.can_read_assets,
                        Primitive.RolePermission.can_get_blocks,
                        Primitive.RolePermission.can_get_roles,
                        Primitive.RolePermission.can_get_all_signatories,
                        Primitive.RolePermission.can_get_domain_signatories,
                        Primitive.RolePermission.can_get_my_signatories,
                        Primitive.RolePermission.can_get_all_txs
                    )
                )
                .createAccount(ChangelogInterface.superuserAccountId, superuserKeyPair.public)
                .createAccount(notaryAccountId, notaryKeyPair.public)
                .appendRole(ChangelogInterface.superuserAccountId, "superuser")
                .appendRole(notaryAccountId, "notary")
                .createAccount(changelogHistoryStorageAccountId, changelogHistoryKeyPair.public)
                .build()
                .build()
        ).build()
    }

    val gson = Gson()
    val changelogExecutor = ChangelogExecutorService(
        ChangelogParser(),
        ChangelogHistoryService(),
        irohaAPI,
        IrohaBatchConsumerDebug(irohaAPI)
    )

    val queryAPI = QueryAPI(
        irohaAPI,
        ChangelogInterface.superuserAccountId,
        superuserKeyPair
    )

    /**
     * Creates test script based changelog request
     */
    fun createChangelogScriptRequest(
        accountName: String,
        schemaVersion: String
    ): ChangelogScriptRequest {
        return ChangelogScriptRequest(
            script = createAccountScript(accountName, schemaVersion),
            details = ChangelogRequestDetails(
                accounts = listOf(
                    ChangelogAccountPublicInfo(
                        DEFAULT_QUORUM,
                        CLIENT_DOMAIN_ID,
                        accountName,
                        listOf(Ed25519Sha3().generateKeypair())
                            .map { keypair -> Utils.toHex(keypair.public.encoded) }
                    )
                ),
                superuserKeys = listOf(HexKeyPair.toHexKeyPair(superuserKeyPair))
            )
        )
    }

    /**
     * Checks if account exists
     * @param accountId - id of account to check
     * @return true if exists
     */
    fun accountExists(accountId: String): Boolean {
        val query = Query.builder(ChangelogInterface.superuserAccountId, 1)
            .getAccount(accountId)
            .buildSigned(superuserKeyPair)
        val response = irohaAPI.query(query)
        return if (response.hasErrorResponse()) {
            if (response.errorResponse.reason == QryResponses.ErrorResponse.Reason.NO_ACCOUNT) {
                false
            } else {
                throw Exception(
                    "Cannot get account $accountId. Error code ${response.errorResponse.errorCode}"
                )
            }
        } else {
            true
        }
    }

    /**
     * Returns script that creates account in Iroha
     * @param accountName - name of account to create in script
     * @param schemaVersion - version of changelog schema
     * @return script
     */
    private fun createAccountScript(accountName: String, schemaVersion: String): String {
        val script = File(SAMPLE_CHANGELOG_PATH).readText()
        return script
            .replace("script_test", accountName)
            .replace("schema_version", schemaVersion)
    }

    override fun close() {
        irohaAPI.close()
        irohaContainer.close()
    }

}
