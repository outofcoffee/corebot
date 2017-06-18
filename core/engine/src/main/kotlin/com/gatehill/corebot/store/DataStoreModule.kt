package com.gatehill.corebot.store

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.gatehill.corebot.config.Settings
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.apache.logging.log4j.LogManager

/**
 * Binds data store implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class DataStoreModule(private val storeName: String) : AbstractModule() {
    private val logger = LogManager.getLogger(DataStoreModule::class.java)

    override fun configure() {
        @Suppress("UNCHECKED_CAST")
        val dataStoreImplClass = ClassLoaderUtil.classLoader
                .loadClass(Settings.dataStores.implementationClass) as Class<DataStore>

        logger.debug("Using '$storeName' data store implementation: ${dataStoreImplClass.canonicalName}")

        bind(DataStore::class.java).annotatedWith(Names.named(storeName)).to(dataStoreImplClass).asSingleton()
    }
}
