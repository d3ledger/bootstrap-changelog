/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.iroha

import com.github.kittinunf.result.Result
import iroha.protocol.TransactionOuterClass
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus

interface IrohaBatchConsumer {

    fun getWaiter(): WaitForTerminalStatus

    fun sendBatchMST(transactions: List<TransactionOuterClass.Transaction>): Result<Unit, Exception>
}

/**
 * Data class that holds information about tx status
 * @param txHash - hash of transaction
 * @param txException - exception that occurs during transaction commitment
 */
data class TxStatus(var txHash: String?, var txException: Exception?) {
    //Checks if tx failed
    fun failed() = txException != null

    companion object {
        fun createEmpty() = TxStatus(null, null)
    }
}
