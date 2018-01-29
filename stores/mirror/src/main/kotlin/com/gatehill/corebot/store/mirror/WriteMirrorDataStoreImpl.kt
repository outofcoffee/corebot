package com.gatehill.corebot.store.mirror

import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import com.gatehill.corebot.store.mirror.config.StoreSettings
import com.gatehill.corebot.util.jsonMapper
import com.google.inject.Injector
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.apache.logging.log4j.LogManager
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * A write mirror data store. Delegates to the backing store as the source of truth,
 * and mirrors writes to another store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class WriteMirrorDataStoreImpl @Inject constructor(injector: Injector) : DataStore {
    private val backingStore: DataStore
    private val mirrorStore: DataStore

    init {
        val classLoader = ClassLoaderUtil.classLoader

        @Suppress("UNCHECKED_CAST")
        backingStore = injector.getInstance(
                classLoader.loadClass(StoreSettings.backingStoreClass) as Class<DataStore>)

        @Suppress("UNCHECKED_CAST")
        mirrorStore = injector.getInstance(
                classLoader.loadClass(StoreSettings.mirrorStoreClass) as Class<DataStore>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V : Any> partitionForClass(partitionId: String, valueClass: KClass<V>): DataStorePartition<K, V> {
        return partitions[partitionId] as DataStorePartition<K, V>?
                ?: createPartition(partitionId, valueClass, loadMirrorClass(valueClass))
    }

    /**
     * If a JsonPath expression is used, the target class must be specified, as it may
     * differ from `valueClass`.
     */
    private fun <M : Any> loadMirrorClass(valueClass: KClass<M>) = StoreSettings.jsonPath?.let {
        ClassLoaderUtil.classLoader.loadClass(StoreSettings.jsonPathTargetClass).kotlin
    } ?: valueClass

    private fun <K, V : Any, M : Any> createPartition(partitionId: String, valueClass: KClass<V>, mirrorClass: KClass<M>) =
            WriteMirrorDataStorePartitionImpl<K, V, M>(
                    backingStore.partitionForClass(partitionId, valueClass),
                    mirrorStore.partitionForClass(partitionId, mirrorClass),
                    mirrorClass.java
            ).apply {
                partitions[partitionId] = this
            }

    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()
}

/**
 * Delegates to the backing store partition as the source of truth, and mirrors writes to another partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class WriteMirrorDataStorePartitionImpl<in K, V, M>(
        private val backingStorePartition: DataStorePartition<K, V>,
        private val mirrorStorePartition: DataStorePartition<K, M>,
        private val mirrorClass: Class<M>) : DataStorePartition<K, V> {

    private val logger = LogManager.getLogger(WriteMirrorDataStorePartitionImpl::class.java)

    override fun set(key: K, value: V) {
        backingStorePartition[key] = value

        try {
            mirrorStorePartition[key] = readMirrorObject(value)
        } catch (e: Exception) {
            logger.warn("Error persisting $key entry to mirror store", e)
        }
    }

    private fun readMirrorObject(value: V): M = StoreSettings.jsonPath?.let {
        // use a JsonPath expression to find the object to mirror
        val jsonFormatted = jsonMapper.convertValue(value, Map::class.java)

        JsonPath.read<Any>(jsonFormatted, StoreSettings.jsonPath)?.let { found ->
            val jsonPathConfig = Configuration.defaultConfiguration().mappingProvider(JacksonMappingProvider(jsonMapper))
            jsonPathConfig.mappingProvider().map(found, mirrorClass, jsonPathConfig)
        }

    } ?: run {
        // use the original value object
        @Suppress("UNCHECKED_CAST")
        value as M
    }

    /**
     * Only writes are mirrored; the backing store is the source of truth.
     */
    override fun get(key: K): V? = backingStorePartition[key]

    override fun remove(key: K) {
        backingStorePartition.remove(key)

        try {
            mirrorStorePartition.remove(key)
        } catch (e: Exception) {
            logger.warn("Error removing $key entry from mirror store", e)
        }
    }
}
