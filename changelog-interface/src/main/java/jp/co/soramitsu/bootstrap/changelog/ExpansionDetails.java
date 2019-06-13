package jp.co.soramitsu.bootstrap.changelog;

import java.util.Map;

/**
 * Details of expansion process
 */
public class ExpansionDetails {
    // Service to expand
    private String service;
    // accountId to expand
    private String accountIdToExpand;
    // New public key
    private String publicKey;
    // New quorum
    private int quorum;
    // Extra data like Ethereum keys and such
    private Map<String, String> additionalData;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

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
                "service='" + service + '\'' +
                ", accountIdToExpand='" + accountIdToExpand + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", quorum=" + quorum +
                ", additionalData=" + additionalData +
                '}';
    }
}
