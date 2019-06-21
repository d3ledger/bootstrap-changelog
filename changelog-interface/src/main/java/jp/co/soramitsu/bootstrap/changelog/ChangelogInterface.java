/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog;

import jp.co.soramitsu.iroha.java.Transaction;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface of changelog scripts
 */
public interface ChangelogInterface {

    // Name of superuser
    String superuserAccount = "superuser";
    // Superuser domain
    String superuserDomain = "bootstrap";
    //Superuser account id
    String superuserAccountId = superuserAccount + "@" + superuserDomain;

    String getSchemaVersion();

    // Main script logic goes here
    Transaction createChangelog(List<ChangelogAccountPublicInfo> accounts, List<ChangelogPeer> peers);

    /**
     * Returns public keys
     *
     * @param accountName - name of account which public keys are requested
     * @param accounts    - list full of account information(name, domain, pubKeys, quorum)
     * @return list of public keys
     * @throws IllegalArgumentException if there is no public keys we are interested in
     */
    @Deprecated
    static List<byte[]> getPubKeys(String accountName, List<ChangelogAccountPublicInfo> accounts) {
        return accounts.stream().filter(
                account -> accountName.equals(account.getAccountName())
        ).findFirst().map(accountPublicInfo -> {
            if (accountPublicInfo.getPubKeys().isEmpty()) {
                throw new IllegalArgumentException("Account " + accountName + " has no pubKeys");
            }
            return accountPublicInfo.getPubKeys().stream().map(DatatypeConverter::parseHexBinary
            ).collect(Collectors.toList());
        }).orElseThrow(() ->
                new IllegalArgumentException("No pubKeys for " + accountName + " was found"));
    }

    /**
     * Returns public keys
     *
     * @param accountId - id of account which public keys are requested
     * @param accounts    - list full of account information(name, domain, pubKeys, quorum)
     * @return list of public keys
     * @throws IllegalArgumentException if there is no public keys we are interested in
     */
    static List<byte[]> getPubKeysByAccountId(String accountId, List<ChangelogAccountPublicInfo> accounts) {
        return accounts.stream().filter(
                account -> accountId.equals(account.getAccountName() + "@" + account.getDomainId())
        ).findFirst().map(accountPublicInfo -> {
            if (accountPublicInfo.getPubKeys().isEmpty()) {
                throw new IllegalArgumentException("Account " + accountId + " has no pubKeys");
            }
            return accountPublicInfo.getPubKeys().stream().map(DatatypeConverter::parseHexBinary
            ).collect(Collectors.toList());
        }).orElseThrow(() ->
                new IllegalArgumentException("No pubKeys for " + accountId + " was found"));
    }
}
