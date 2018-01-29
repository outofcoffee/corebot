package com.gatehill.corebot.store.rest.config

import com.gatehill.corebot.config.EnvironmentSettings

/**
 * Data store settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object StoreSettings : EnvironmentSettings() {
    val baseUrl: String by lazy { getenv("REST_BASE_URL") ?: throw IllegalStateException("Missing REST base URL") }

    val resource: String by lazy { getenv("REST_RESOURCE") ?: throw IllegalStateException("Missing REST resource") }

    /**
     * Single value that represents the key (essentially the 'primary key') to map to the store's key.
     */
    val keyField: String by lazy { getenv("REST_KEY_FIELD") ?: throw IllegalStateException("Missing REST key field") }

    /**
     * Comma separated list of `key=value` elements that maps field names to body fields.
     *
     * Example: `foo=bar,baz=qux`
     */
    val valueMap: Map<String, String> by lazy {
        getenv("REST_VALUE_MAP")
                ?.split(",")
                ?.map { it.split("=").let { element -> element[0] to element[1] } }
                ?.toMap()
                ?: throw IllegalStateException("Missing REST value map")
    }
}
