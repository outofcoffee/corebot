package com.gatehill.corebot.store.redis.config

import com.gatehill.corebot.config.EnvironmentSettings

/**
 * Data store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings : EnvironmentSettings() {
    val redisHost by lazy { getenv("REDIS_HOST") ?: "localhost" }
    val redisPort by lazy { getenv("REDIS_PORT")?.toInt() ?: 6379 }
}
