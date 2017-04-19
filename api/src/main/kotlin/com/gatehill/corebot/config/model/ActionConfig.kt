package com.gatehill.corebot.config.model

private val defaultDriver = "rundeck"

/**
 * Models an action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ActionConfig(val template: String,
                   val jobId: String,
                   name: String?,
                   options: Map<String, OptionConfig>?,
                   tags: List<String>?, driver: String?,
                   showJobOutput: String?, showJobOutcome: String?) {

    val name: String
    val options: Map<String, OptionConfig>
    val tags: List<String>
    val driver: String
    val showJobOutput: String
    val showJobOutcome: String

    init {
        this.name = name ?: ""
        this.options = options ?: emptyMap()
        this.tags = tags ?: emptyList()
        this.driver = driver ?: defaultDriver
        this.showJobOutput = showJobOutput ?: "false"
        this.showJobOutcome = showJobOutcome ?: "true"
    }

    override fun toString(): String {
        return "ActionConfig(name='$name', jobId='$jobId', options=$options, template='$template', tags=$tags, " +
                "driver='$driver', showJobOutput='$showJobOutput', showJobOutcome='$showJobOutcome')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ActionConfig

        if (name != other.name) return false

        return true
    }

    /**
     * Name uniquely identifies an action configuration.
     */
    override fun hashCode() = name.hashCode()
}

/**
 * Convenience method to read an attribute from an ActionConfig.
 */
fun readActionConfigAttribute(actions: List<ActionConfig>, supplier: (ActionConfig) -> String): String {
    val names = StringBuilder()

    actions.forEach { action ->
        if (names.isNotEmpty()) names.append(", ")
        names.append(supplier(action))
    }

    return names.toString()
}
