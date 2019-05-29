/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.dto

import jp.co.soramitsu.bootstrap.changelog.ChangelogAccountPublicInfo
import jp.co.soramitsu.bootstrap.changelog.ChangelogPeer
import jp.co.soramitsu.iroha.java.Utils
import java.security.KeyPair

/**
 * Changelog request details data
 * @param accounts - accounts(with corresponding public keys) that are needed to execute changelog
 * @param peers - peers that are needed to execute changelog
 * @param superuserKeys - keys of superuser account
 */
data class ChangelogRequestDetails(
    val accounts: List<ChangelogAccountPublicInfo> = emptyList(),
    val peers: List<ChangelogPeer> = emptyList(),
    val superuserKeys: List<HexKeyPair> = emptyList()
)

/**
 * Keypair in hex encoding
 * @param pubKey - public key
 * @param privKey - private key
 */
data class HexKeyPair(val pubKey: String, val privKey: String) {
    /**
     * Maps to Iroha keypair
     */
    fun toIrohaKeyPair() = Utils.parseHexKeypair(pubKey, privKey)!!

    companion object {

        /**
         * Maps Iroha keypair to HexKeyPair
         * @param keyPair - Iroha keypair to map
         * @return HexKeyPair
         */
        fun toHexKeyPair(keyPair: KeyPair) =
            HexKeyPair(
                Utils.toHex(keyPair.public.encoded),
                Utils.toHex(keyPair.private.encoded)
            )
    }
}

/**
 * Changelog file based request data
 * @param changelogFile - path to changelog script file
 * @param details -details of request
 */
data class ChangelogFileRequest(
    val changelogFile: String,
    val details: ChangelogRequestDetails
)

/**
 * Changelog script based request data
 * @param script - changelog groovy script
 * @param details - details of request
 */
data class ChangelogScriptRequest(
    val script: String,
    val details: ChangelogRequestDetails
)

/**
 * Response of changelog
 * @param message - plain message
 */
data class ChangelogResponse(val message: String) {
    companion object {
        /**
         * Factory function
         * @return successful response
         */
        fun ok() = ChangelogResponse("OK")

        /**
         * Factory function
         * @return response with exception message
         */
        fun exception(ex: Exception) = ChangelogResponse(
            if (ex.message != null) {
                ex.message!!
            } else {
                "Error"
            }
        )
    }
}
