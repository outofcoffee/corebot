package com.gatehill.corebot.config.model

/**
 * Models an action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class ActionConfig(val template: String,
                        val jobId: String,
                        val name: String,
                        val options: Map<String, OptionConfig>,
                        val tags: List<String>,
                        val driver: String,
                        val showJobOutput: Boolean,
                        val showJobOutcome: Boolean,
                        val runAsTriggerUser: Boolean) {

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
