/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package jp.co.soramitsu.bootstrap.changelog.config

import com.d3.commons.config.loadRawLocalConfigs
import jp.co.soramitsu.iroha.java.IrohaAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

val changelogConfig =
    loadRawLocalConfigs(
        prefix = "changelog",
        type = ChangelogConfig::class.java,
        filename = "changelog.properties"
    )

@Configuration
class ChangelogAppConfiguration {

    @Bean
    fun irohaAPI() = IrohaAPI(changelogConfig.irohaHost, changelogConfig.irohaPort)

}
