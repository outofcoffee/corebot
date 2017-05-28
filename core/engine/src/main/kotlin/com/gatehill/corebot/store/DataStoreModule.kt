package com.gatehill.corebot.store

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.config.Settings
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.apache.logging.log4j.LogManager

/**
 * Binds data store implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class DataStoreModule : AbstractModule() {
    private val logger = LogManager.getLogger(DataStoreModule::class.java)

    override fun configure() {
        val dataStoreImplClass = Settings.dataStores.implementationClass
        logger.debug("Using data store implementation: $dataStoreImplClass")

        bind(DataStore::class.java).annotatedWith(Names.named("lockStore"))
                .to(dataStoreImplClass).asSingleton()
    }
}
