package com.gatehill.corebot.config.model

/**
 * A deserialised action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class DeserialisedActionConfig(template: String?,
                               jobId: String?,
                               name: String?,
                               options: Map<String, OptionConfig>?,
                               tags: List<String>?,
                               val driver: String?,
                               val showJobOutput: Boolean?,
                               val showJobOutcome: Boolean?,
                               val runAsTriggerUser: Boolean?) {

    val template: String = template ?: ""
    val jobId: String = jobId ?: ""
    val name: String = name ?: ""
    val options: Map<String, OptionConfig> = options ?: emptyMap()
    val tags: List<String> = tags ?: emptyList()

    override fun toString(): String {
        return "DeserialisedActionConfig(name='$name', jobId='$jobId', options=$options, template='$template', tags=$tags, " +
                "driver='$driver', showJobOutput='$showJobOutput', showJobOutcome='$showJobOutcome', runAsTriggerUser='$runAsTriggerUser')"
    }
}
