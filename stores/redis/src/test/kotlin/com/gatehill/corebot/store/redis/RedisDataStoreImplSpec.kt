package com.gatehill.corebot.store.redis

import com.gatehill.corebot.store.partition
import com.gatehill.corebot.store.redis.config.StoreSettings
import com.gatehill.corebot.test.KRedisContainer
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `RedisDataStoreImpl`.
 */
object RedisDataStoreImplSpec : Spek({
    given("a Redis store") {
        val redis = KRedisContainer()

        beforeGroup {
            redis.start()

            StoreSettings.env = mapOf(
                    "REDIS_HOST" to redis.containerIpAddress,
                    "REDIS_PORT" to redis.getMappedPort(6379).toString()
            )
        }

        on("getting a partition") {
            val store = RedisDataStoreImpl()
            val partition = store.partition<String, Example>("test")

            it("saves objects") {
                partition["key"] = Example("value")
            }

            it("loads saved objects") {
                val value = partition["key"]
                value `should equal` value
            }

            it("removes saved objects") {
                partition.remove("key")
                partition["key"].`should be null`()
            }
        }

        afterGroup {
            redis.stop()
        }
    }
})

data class Example(val name: String)
