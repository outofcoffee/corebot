package com.gatehill.corebot.store

import kotlin.reflect.KClass

/**
 * Manages data store partitions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface DataStore {
    fun <K, V: Any> partitionForClass(partitionId: String, valueClass: KClass<V>): DataStorePartition<K, V>
}

/**
 * Syntactic sugar, which calls `DataStore.partitionForClass()` using the reified
 * type as the value class.
 */
inline fun <K, reified V: Any> DataStore.partition(partitionId: String) =
        this.partitionForClass<K, V>(partitionId, V::class)

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
