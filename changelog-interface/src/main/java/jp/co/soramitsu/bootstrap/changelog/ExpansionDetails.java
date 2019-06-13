package jp.co.soramitsu.bootstrap.changelog;

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
    // Number of peers before expansion
    private int peersBeforeExpansion;

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

    public int getPeersBeforeExpansion() {
        return peersBeforeExpansion;
    }

    public void setPeersBeforeExpansion(int peersBeforeExpansion) {
        this.peersBeforeExpansion = peersBeforeExpansion;
    }

    @Override
    public String toString() {
        return "ExpansionDetails{" +
                "service='" + service + '\'' +
                ", accountIdToExpand='" + accountIdToExpand + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", quorum=" + quorum +
                ", peersBeforeExpansion=" + peersBeforeExpansion +
                '}';
    }
}
