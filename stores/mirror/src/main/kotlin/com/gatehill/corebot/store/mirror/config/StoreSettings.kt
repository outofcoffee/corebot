package com.gatehill.corebot.store.mirror.config

import com.gatehill.corebot.config.EnvironmentSettings

/**
 * Mirror store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings : EnvironmentSettings() {
    val backingStoreClass: String by lazy {
        getenv("MIRRORED_BACKING_STORE_IMPL")
                ?: throw IllegalArgumentException("Missing mirrored backing store implementation")
    }

    val mirrorStoreClass: String by lazy {
        getenv("MIRROR_DATA_STORE_IMPL")
                ?: throw IllegalArgumentException("Missing mirror data store implementation")
    }

    /**
     * Optional JsonPath to identify target to persist.
     * Doesn't use `lazy` to avoid caching issues when value changes.
     */
    val jsonPath: String?
        get() = getenv("MIRROR_JSON_PATH")

    /**
     * Fully qualified class representing the target of the JsonPath.
     * Doesn't use `lazy` to avoid caching issues when value changes.
     */
    val jsonPathTargetClass: String
        get() = getenv("MIRROR_JSON_PATH_TARGET") ?: throw IllegalArgumentException("Missing JsonPath target class")
}
