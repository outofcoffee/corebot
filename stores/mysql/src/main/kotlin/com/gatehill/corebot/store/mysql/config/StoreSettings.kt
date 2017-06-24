package com.gatehill.corebot.store.mysql.config

/**
 * Data store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings {
    val connectionString by lazy { System.getenv("MYSQL_CONNECTION_STRING") ?: "jdbc:mysql://localhost:3306/corebot" }
    val username by lazy { System.getenv("MYSQL_USERNAME") ?: "corebot" }
    val password by lazy { System.getenv("MYSQL_PASSWORD") ?: "Corebot123!" }
}
