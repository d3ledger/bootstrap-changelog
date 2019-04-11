package jp.co.soramitsu.bootstrap.changelog.config

import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.ProxyConfigProvider
import com.jdiazcano.cfg4k.sources.ConfigSource
import java.io.File
import java.io.InputStream

/**
 * Returns config folder
 */
fun getConfigFolder() = System.getProperty("user.dir") + "/configs"

private class Stream(private val stream: InputStream) : ConfigSource {
    override fun read(): InputStream {
        return stream
    }
}

/**
 * Loads config from .properties file
 * @param prefix - prefix of config file
 * @param type - type of config file
 * @param filename - name of config file
 * @return config object
 */
fun <T : Any> loadConfigs(prefix: String, type: Class<T>, filename: String): T {
    File("${getConfigFolder()}/$filename").inputStream().use { inputStream ->
        val stream = Stream(inputStream)
        val configLoader = PropertyConfigLoader(stream)
        val provider = ProxyConfigProvider(configLoader)
        return provider.bind(prefix, type)
    }
}
