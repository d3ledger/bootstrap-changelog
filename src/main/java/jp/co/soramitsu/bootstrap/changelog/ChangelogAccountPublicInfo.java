package jp.co.soramitsu.bootstrap.changelog;

import java.util.List;

public class ChangelogAccountPublicInfo {

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
