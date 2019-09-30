/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.iroha

import iroha.protocol.Endpoint
import jp.co.soramitsu.iroha.java.IrohaAPI
import jp.co.soramitsu.iroha.java.subscription.WaitForTerminalStatus
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Class that is responsible for sending MST batches to Iroha in debug environment
 */
@Component
@Profile("debug")
class IrohaBatchConsumerDebug(irohaAPI: IrohaAPI) : IrohaBatchConsumerProd(irohaAPI) {

    /**
     * Statuses that we consider terminal
     */
    private val terminalStatusesMST =
        listOf(
            //TODO this is the only difference between 'prod'
            //Endpoint.TxStatus.MST_PENDING,
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
}