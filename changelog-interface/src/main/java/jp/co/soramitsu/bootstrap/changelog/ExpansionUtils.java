/*
 * Copyright D3 Ledger, Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog;

import com.google.gson.Gson;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.Utils;

public class ExpansionUtils {

    private static final Gson gson = new Gson();

    private ExpansionUtils() {

    }

    /**
     * Creates transaction that triggers the expansion process
     *
     * @param expansionDetails - details of expansion
     * @param triggerAccountId - account that is used as a trigger
     * @return unsigned transaction
     */
    public static Transaction createExpansionTriggerTx(
            ExpansionDetails expansionDetails,
            String triggerAccountId) {
        return Transaction
                .builder(ChangelogInterface.superuserAccountId)
                .setAccountDetail(
                        triggerAccountId,
                        String.valueOf(System.currentTimeMillis()),
                        Utils.irohaEscape(gson.toJson(expansionDetails))
                ).build();
    }
}
