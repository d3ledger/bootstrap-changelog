/*
 * Copyright D3 Ledger, Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog;

import java.io.Serializable;
import java.util.List;

public class ChangelogAccountPublicInfo implements Serializable {

    private static final long serialVersionUID = 7500670093040730734L;

    //For serialization
    public ChangelogAccountPublicInfo() {

    }

    public ChangelogAccountPublicInfo(
            int quorum,
            String domainId,
            String accountName,
            List<String> pubKeys) {
        this.quorum = quorum;
        this.domainId = domainId;
        this.accountName = accountName;
        this.pubKeys = pubKeys;
    }

    private int quorum;
    private String domainId;
    private String accountName;
    private List<String> pubKeys;

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public List<String> getPubKeys() {
        return pubKeys;
    }

    public void setPubKeys(List<String> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public String getId() {
        return accountName + "@" + domainId;
    }
}
