package com.gatehill.corebot.store.impl

import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition

/**
 * A simple in-memory store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class InMemoryDataStoreImpl : DataStore {
    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> partition(partitionId: String): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>? ?:
                    InMemoryDataStorePartitionImpl<K, V>().apply { partitions[partitionId] = this }
}

/**
 * A simple in-memory partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class InMemoryDataStorePartitionImpl<in K, V> : DataStorePartition<K, V> {
    private val data = mutableMapOf<K, V>()

    override fun set(key: K, value: V) {
        data[key] = value
    }

    override fun get(key: K): V? = data[key]

    override fun remove(key: K) {
        data.remove(key)
    }
}
