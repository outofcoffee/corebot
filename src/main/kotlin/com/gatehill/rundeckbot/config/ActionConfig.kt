package com.gatehill.rundeckbot.config

/**
 * Models a action configuration.
 */
data class ActionConfig(var name: String?,
                        val jobId: String,
                        val options: Map<String, String>?,
                        val template: String,
                        val tags: List<String>?)
