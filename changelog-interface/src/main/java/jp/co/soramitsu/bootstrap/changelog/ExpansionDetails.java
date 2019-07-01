/*
 * Copyright D3 Ledger, Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog;

import java.util.Map;

/**
 * Details of expansion process
 */
public class ExpansionDetails {
    // accountId to expand
    private String accountIdToExpand;
    // New public key
    private String publicKey;
    // New quorum
    private int quorum;
    // Extra data like Ethereum keys and such
    private Map<String, String> additionalData;

    public String getAccountIdToExpand() {
        return accountIdToExpand;
    }

    public void setAccountIdToExpand(String accountIdToExpand) {
        this.accountIdToExpand = accountIdToExpand;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "ExpansionDetails{" +
                "accountIdToExpand='" + accountIdToExpand + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", quorum=" + quorum +
                ", additionalData=" + additionalData +
                '}';
    }
}
