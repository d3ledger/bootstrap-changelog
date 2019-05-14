package jp.co.soramitsu.bootstrap.changelog.config

import com.d3.commons.config.loadConfigs
import jp.co.soramitsu.iroha.java.IrohaAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

val changelogConfig =
    loadConfigs(
        prefix = "changelog",
        type = ChangelogConfig::class.java,
        filename = "/changelog.properties"
    ).get()

@Configuration
class ChangelogAppConfiguration {

    @Bean
    fun irohaAPI() = IrohaAPI(changelogConfig.irohaHost, changelogConfig.irohaPort)

}
