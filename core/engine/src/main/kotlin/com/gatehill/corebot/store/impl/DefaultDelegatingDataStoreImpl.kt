package com.gatehill.corebot.store.impl

import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import javax.inject.Inject
import javax.inject.Provider

/**
 * Delegates to the default data store implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class DefaultDelegatingDataStoreImpl @Inject constructor(
        private val inMemoryStoreProvider: Provider<InMemoryDataStoreImpl>) : DataStore {

    /**
     * Singleton store.
     */
    private val inMemoryDataStore by lazy { inMemoryStoreProvider.get() }

    override fun <K, V> partition(partitionId: String): DataStorePartition<K, V> =
            inMemoryDataStore.partition(partitionId)
}
