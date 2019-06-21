/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.iroha

import com.github.kittinunf.result.Result
import iroha.protocol.Endpoint
import iroha.protocol.TransactionOuterClass
import jp.co.soramitsu.iroha.java.IrohaAPI
import jp.co.soramitsu.iroha.java.TransactionStatusObserver
import jp.co.soramitsu.iroha.java.Utils
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus
import mu.KLogging
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeoutException


private val logger = KLogging().logger
/**
 * Statuses that we consider terminal
 */
private val terminalStatusesMST =
    Arrays.asList(
        Endpoint.TxStatus.MST_PENDING,
        Endpoint.TxStatus.STATELESS_VALIDATION_FAILED,
        Endpoint.TxStatus.STATEFUL_VALIDATION_FAILED,
        Endpoint.TxStatus.COMMITTED,
        Endpoint.TxStatus.MST_EXPIRED,
        //We don't consider this status terminal on purpose
        //Endpoint.TxStatus.NOT_RECEIVED,
        Endpoint.TxStatus.REJECTED,
        Endpoint.TxStatus.UNRECOGNIZED
    )

private val waitForTerminalStatusMST = WaitForTerminalStatus(terminalStatusesMST)

/**
 * Create tx status observer
 * @param txStatus - object that will hold tx status after observer completion
 * @return tx status observer
 */
private fun createTxStatusObserverMST(txStatus: TxStatus): InlineTransactionStatusObserver {
    return TransactionStatusObserver.builder()
        .onError { ex -> txStatus.txException = IllegalStateException(ex) }
        .onMstExpired { expiredTx ->
            txStatus.txException =
                    TimeoutException("Tx ${expiredTx.txHash} MST expired. ${expiredTx.errOrCmdName}")
        }
        .onNotReceived { failedTx ->
            txStatus.txException =
                    IOException("Tx ${failedTx.txHash} was not received. ${failedTx.errOrCmdName}")
        }
        .onRejected { rejectedTx ->
            txStatus.txException =
                    IOException("Tx ${rejectedTx.txHash} was rejected. ${rejectedTx.errOrCmdName}")
        }
        .onTransactionFailed { failedTx ->
            txStatus.txException = Exception("Tx ${failedTx.txHash} failed. ${failedTx.errOrCmdName}")
        }
        .onUnrecognizedStatus { failedTx ->
            txStatus.txException =
                    Exception("Tx ${failedTx.txHash} got unrecognized status. ${failedTx.errOrCmdName}")
        }
        .onTransactionCommitted { successTx -> txStatus.txHash = successTx.txHash.toUpperCase() }
        .onMstPending { pendingTx -> txStatus.txHash = pendingTx.txHash.toUpperCase() }
        .build()
}

/**
 * Send signed batch transaction to Iroha
 * @param transactions - transactions to send
 */
fun IrohaAPI.sendBatchMST(transactions: List<TransactionOuterClass.Transaction>): Result<Unit, Exception> {
    val hashes = getHashes(transactions)
    logger.info("Send batch MST: $hashes")
    return Result.of {
        this.transactionListSync(transactions)
        transactions.map { tx -> Utils.hash(tx) }.forEach { txHash ->
            val txStatus = TxStatus.createEmpty()
            logger.info("Wait terminal statuses: $hashes")
            waitForTerminalStatusMST.subscribe(this, txHash)
                .blockingSubscribe(createTxStatusObserverMST(txStatus))
            if (txStatus.failed()) {
                throw Exception("Iroha batch error", txStatus.txException)
            }
        }
    }
}

/**
 * Send signed transaction to Iroha
 * @param transaction - transaction to send
 */
fun IrohaAPI.send(transaction: TransactionOuterClass.Transaction): Result<Unit, Exception> {
    val txHash = Utils.hash(transaction)
    logger.info("Send tx:${Utils.toHex(txHash)}")
    return Result.of {
        this.transactionSync(transaction)
        val txStatus = TxStatus.createEmpty()
        logger.info("Wait terminal statuses: ${Utils.toHex(txHash)}")
        waitForTerminalStatusMST.subscribe(this, txHash)
            .blockingSubscribe(createTxStatusObserverMST(txStatus))
        if (txStatus.failed()) {
            throw Exception("Iroha batch error", txStatus.txException)
        }
    }
}

/**
 * Returns hashes of given transactions in HEX format
 * @param transactions - transactions that will be used to get hashes
 * @return transaction hashes in HEX format
 */
fun getHashes(transactions: List<TransactionOuterClass.Transaction>) =
    transactions.map { tx -> Utils.toHex(Utils.hash(tx)) }

/**
 * Data class that holds information about tx status
 * @param txHash - hash of transaction
 * @param txException - exception that occurs during transaction commitment
 */
private data class TxStatus(var txHash: String?, var txException: Exception?) {
    //Checks if tx failed
    fun failed() = txException != null

    companion object {
        fun createEmpty() = TxStatus(null, null)
    }
}
