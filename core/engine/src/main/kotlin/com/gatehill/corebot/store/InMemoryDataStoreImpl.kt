package com.gatehill.corebot.store

import kotlin.reflect.KClass

/**
 * A simple in-memory store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class InMemoryDataStoreImpl : DataStore {
    @Suppress("UNCHECKED_CAST")
    override fun <K, V : Any> partitionForClass(partitionId: String, valueClass: KClass<V>): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>?
                    ?: InMemoryDataStorePartitionImpl<K, V>().apply { partitions[partitionId] = this }

    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()
}

/**
 * A simple in-memory partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class InMemoryDataStorePartitionImpl<in K, V> : DataStorePartition<K, V> {
    private val data = mutableMapOf<K, V>()

    override fun set(key: K, value: V) {
        data[key] = value
    }

    override fun get(key: K): V? = data[key]

    override fun remove(key: K) {
        data.remove(key)
    }
}
