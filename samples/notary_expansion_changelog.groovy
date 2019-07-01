/*
 * Copyright D3 Ledger, Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import jp.co.soramitsu.bootstrap.changelog.ChangelogAccountPublicInfo
import jp.co.soramitsu.bootstrap.changelog.ChangelogInterface
import jp.co.soramitsu.bootstrap.changelog.ChangelogPeer
import jp.co.soramitsu.iroha.java.Transaction

import java.security.Permission

// Notary expansion changelog. Sets notary@notary account quorum to 2
class NotaryExpansionChangeLog implements ChangelogInterface {

    String getSchemaVersion() {
        return "notary_expansion"
    }

    Transaction createChangelog(List<ChangelogAccountPublicInfo> accounts,
                                List<ChangelogPeer> peers) {
        def accountId = "notary@notary"
        def pubKey = getPubKeysByAccountId(
                accountId, accounts).first()
        def expandNotaryTransaction = Transaction.builder(superuserAccountId)
                .addSignatory(accountId, pubKey)
                .setAccountQuorum(accountId, 2)
        return expandNotaryTransaction.build()
    }
}
