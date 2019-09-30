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
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * Class that is responsible for sending MST batches to Iroha in production environment
 */
@Component
@Profile("prod")
class IrohaBatchConsumerProd(private val irohaAPI: IrohaAPI) : IrohaBatchConsumer {

    /**
     * Statuses that we consider terminal
     */
    private val terminalStatusesMST =
        listOf(
            Endpoint.TxStatus.MST_PENDING,
            Endpoint.TxStatus.STATELESS_VALIDATION_FAILED,
            Endpoint.TxStatus.STATEFUL_VALIDATION_FAILED,
            Endpoint.TxStatus.COMMITTED,
            Endpoint.TxStatus.MST_EXPIRED,
            Endpoint.TxStatus.REJECTED,
            Endpoint.TxStatus.UNRECOGNIZED
        )

    private val waitForTerminalStatusMST = WaitForTerminalStatus(terminalStatusesMST)

    /**
     * Returns Iroha tx waiter object
     */
    override fun getWaiter() = waitForTerminalStatusMST

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
    override fun sendBatchMST(transactions: List<TransactionOuterClass.Transaction>): Result<Unit, Exception> {
        val hashes = getHashes(transactions)
        logger.info("Send batch MST: $hashes")
        return Result.of {
            irohaAPI.transactionListSync(transactions)
            transactions.map { tx -> Utils.hash(tx) }.forEach { txHash ->
                val txStatus = TxStatus.createEmpty()
                logger.info("Wait terminal statuses: $hashes")
                getWaiter().subscribe(irohaAPI, txHash)
                    .blockingSubscribe(createTxStatusObserverMST(txStatus))
                if (txStatus.failed()) {
                    throw Exception("Iroha batch error", txStatus.txException)
                }
            }
        }
    }

    /**
     * Returns hashes of given transactions in HEX format
     * @param transactions - transactions that will be used to get hashes
     * @return transaction hashes in HEX format
     */
    private fun getHashes(transactions: List<TransactionOuterClass.Transaction>) =
        transactions.map { tx -> Utils.toHex(Utils.hash(tx)) }

    companion object : KLogging()
}
