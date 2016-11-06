package com.gatehill.rundeckbot.config.model

/**
 * Models configuration for action options.
 */
data class OptionConfig(val static: Map<String, String>? = emptyMap(),
                        val transformers: Map<String, List<TransformType>>? = emptyMap()) {

}

/**
 * The supported transformers.
 */
enum class TransformType {
    LOWERCASE,
    UPPERCASE
}
