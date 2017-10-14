package com.gatehill.corebot.store.mysql.config

import com.gatehill.corebot.config.EnvironmentSettings

/**
 * Data store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings : EnvironmentSettings() {
    val connectionString by lazy { getenv("MYSQL_CONNECTION_STRING") ?: "jdbc:mysql://localhost:3306/corebot" }
    val username by lazy { getenv("MYSQL_USERNAME") ?: "corebot" }
    val password by lazy { getenv("MYSQL_PASSWORD") ?: "Corebot123!" }
}
