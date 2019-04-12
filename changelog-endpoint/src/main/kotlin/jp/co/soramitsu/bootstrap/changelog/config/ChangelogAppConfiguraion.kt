package jp.co.soramitsu.bootstrap.changelog.config

import jp.co.soramitsu.iroha.java.IrohaAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChangelogAppConfiguraion {

    private val changelogConfig =
        loadConfigs("changelog", ChangelogConfig::class.java, "/changelog/changelog.properties")

    @Bean
    fun irohaAPI() = IrohaAPI(changelogConfig.irohaHost, changelogConfig.irohaPort)

}
