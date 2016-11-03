package com.gatehill.rundeckbot.config.model

/**
 * Models an action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class ActionConfig(val jobId: String,
                        val options: Map<String, String>?,
                        val template: String,
                        val tags: List<String>?,
                        val transforms: Map<String, List<TransformType>>?) {

    lateinit var name: String
}

/**
 * The supported transforms.
 */
enum class TransformType {
    LOWERCASE,
    UPPERCASE
}

/**
 * Convenience method to read an attribute from an ActionConfig.
 */
fun readActionConfigAttribute(actions: List<ActionConfig>, supplier: (ActionConfig) -> String): String {
    val names = StringBuilder()

    actions.forEach { action ->
        if (names.length > 0) names.append(", ")
        names.append(supplier(action))
    }

    return names.toString()
}
