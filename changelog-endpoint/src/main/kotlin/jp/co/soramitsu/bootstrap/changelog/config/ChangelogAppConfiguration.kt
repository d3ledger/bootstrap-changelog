package jp.co.soramitsu.bootstrap.changelog.config

import jp.co.soramitsu.iroha.java.IrohaAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChangelogAppConfiguration {

    private val changelogConfig =
        loadConfigs(
            prefix = "changelog",
            type = ChangelogConfig::class.java,
            filename = "/changelog/changelog.properties"
        )

    @Bean
    fun irohaAPI() = IrohaAPI(changelogConfig.irohaHost, changelogConfig.irohaPort)

}
