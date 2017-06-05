package com.gatehill.corebot.config

import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.InMemoryDataStoreImpl
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Engine settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object Settings {
    private val logger: Logger = LogManager.getLogger(Settings::class.java)

    class Execution {
        val executionTimeout by lazy { (System.getenv("EXECUTION_STATUS_TIMEOUT")?.toInt() ?: 120) * 1000 }
    }

    val execution = Execution()

    class DataStores {
        /**
         * The implementation for the `DataStore`.
         */
        @Suppress("UNCHECKED_CAST")
        val implementationClass = Class.forName(System.getenv("DATA_STORE_IMPL") ?:
                InMemoryDataStoreImpl::class.java.canonicalName) as Class<DataStore>
    }

    val dataStores = DataStores()

    class Chat {
        val chatGenerator: InputStream = System.getenv("CHAT_GENERATOR_FILE")
                ?.let { Files.newInputStream(Paths.get(it)) }
                ?: this.javaClass.getResourceAsStream("/default-chat.yml")
    }

    val chat = Chat()

    /**
     * The file containing the action configuration.
     */
    val actionConfigFile by lazy {
        val configFile: String? = System.getenv("BOT_CONFIG")?.apply {
            logger.warn("Variable 'BOT_CONFIG' is deprecated and will be removed in a future release - use 'ACTION_CONFIG_FILE' or 'ACTION_CONFIG' instead")
        } ?: System.getenv("ACTION_CONFIG_FILE")

        File(configFile ?: "/opt/corebot/actions.yml")
    }
    val configCacheSecs by lazy { System.getenv("CACHE_EXPIRY")?.toLong() ?: 60L }
}
