package com.gatehill.corebot.store.redis.config

/**
 * Data store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings {
    val redisHost by lazy { System.getenv("REDIS_HOST") ?: "localhost" }
    val redisPort by lazy { System.getenv("REDIS_PORT")?.toInt() ?: 6379 }
}
