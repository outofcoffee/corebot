package com.gatehill.corebot.config.model

private val defaultDriver = "rundeck"

/**
 * Models an action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ActionConfig(template: String?,
                   jobId: String?,
                   name: String?,
                   options: Map<String, OptionConfig>?,
                   tags: List<String>?,
                   driver: String?,
                   showJobOutput: Boolean?,
                   showJobOutcome: Boolean?,
                   runAsTriggerUser: Boolean?) {

    val template: String = template ?: ""
    val jobId: String = jobId ?: ""
    val name: String = name ?: ""
    val options: Map<String, OptionConfig> = options ?: emptyMap()
    val tags: List<String> = tags ?: emptyList()
    val driver: String = driver ?: defaultDriver
    val showJobOutput: Boolean = showJobOutput ?: false
    val showJobOutcome: Boolean = showJobOutcome ?: true
    val runAsTriggerUser: Boolean = runAsTriggerUser ?: false

    override fun toString(): String {
        return "ActionConfig(name='$name', jobId='$jobId', options=$options, template='$template', tags=$tags, " +
                "driver='$driver', showJobOutput='$showJobOutput', showJobOutcome='$showJobOutcome', runAsTriggerUser='$runAsTriggerUser')"
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
