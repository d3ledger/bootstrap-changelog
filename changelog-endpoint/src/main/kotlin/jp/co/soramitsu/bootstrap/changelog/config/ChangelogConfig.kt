package jp.co.soramitsu.bootstrap.changelog.config

/**
 * Changelog configuration interface
 */
interface ChangelogConfig {
    // Host of Iroha node
    val irohaHost: String
    // Port of Iroha node
    val irohaPort: Int
}
