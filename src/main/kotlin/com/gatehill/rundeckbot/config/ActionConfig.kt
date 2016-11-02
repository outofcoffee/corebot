package com.gatehill.rundeckbot.config

/**
 * Models an action configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class ActionConfig(val jobId: String,
                        val options: Map<String, String>?,
                        val template: String,
                        val tags: List<String>?) {

    lateinit var name: String
}
