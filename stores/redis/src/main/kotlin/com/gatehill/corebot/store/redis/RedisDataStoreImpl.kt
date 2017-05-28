package com.gatehill.corebot.store.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import com.gatehill.corebot.store.redis.config.StoreSettings
import redis.clients.jedis.Jedis

/**
 * A Redis store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class RedisDataStoreImpl : DataStore {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> partition(partitionId: String, clazz: Class<V>): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>? ?:
                    RedisDataStorePartitionImpl<K, V>(mapper, clazz, partitionId).apply { partitions[partitionId] = this }
}

/**
 * A Redis partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class RedisDataStorePartitionImpl<in K, V>(private val mapper: ObjectMapper,
                                                   private val clazz: Class<V>,
                                                   private val partitionId: String) : DataStorePartition<K, V> {

    private val db by lazy {
        Jedis(StoreSettings.redisHost, StoreSettings.redisPort)
    }

    override fun set(key: K, value: V) {
        db.set(buildKey(key), mapper.writeValueAsString(value))
    }

    override fun get(key: K): V? = db.get(buildKey(key))?.let {
        mapper.readValue(it, clazz)
    }

    override fun remove(key: K) {
        db.del(buildKey(key))
    }

    private fun buildKey(key: K): String = "$partitionId.$key"
}
