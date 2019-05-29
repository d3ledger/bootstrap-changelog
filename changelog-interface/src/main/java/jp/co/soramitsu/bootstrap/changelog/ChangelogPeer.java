/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog;

public class ChangelogPeer {

    private String peerKey;
    private String hostPort;

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
}
