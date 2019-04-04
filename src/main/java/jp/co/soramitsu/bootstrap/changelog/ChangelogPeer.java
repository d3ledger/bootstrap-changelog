package jp.co.soramitsu.bootstrap.changelog;

public class ChangelogPeer {

    private String peerKey;
    private String hostPort;
    private String notaryHostPort;

    public String getPeerKey() {
        return peerKey;
    }

    public void setPeerKey(String peerKey) {
        this.peerKey = peerKey;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getNotaryHostPort() {
        return notaryHostPort;
    }

    public void setNotaryHostPort(String notaryHostPort) {
        this.notaryHostPort = notaryHostPort;
    }
}
