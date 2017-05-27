package com.gatehill.corebot.store

/**
 * Manages data store partitions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface DataStore {
    fun <K, V> partition(partitionId: String, clazz: Class<V>): DataStorePartition<K, V>
}

/**
 * Stores a particular type of data in a store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface DataStorePartition<in K, V> {
    operator fun set(key: K, value: V)
    operator fun get(key: K): V?
    fun remove(key: K)
}
