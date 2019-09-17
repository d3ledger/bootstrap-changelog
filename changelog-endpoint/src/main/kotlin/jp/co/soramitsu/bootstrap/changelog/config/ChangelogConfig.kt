/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.config

/**
 * Changelog configuration interface
 */
interface ChangelogConfig {
    // Host of Iroha node
    val irohaHost: String
    // Port of Iroha node
    val irohaPort: Int
    // Port of changelog
    val port: Int
}
