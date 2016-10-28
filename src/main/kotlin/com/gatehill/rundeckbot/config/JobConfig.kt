package com.gatehill.rundeckbot.config

/**
 * Models a job configuration.
 */
data class JobConfig(var name: String?,
                     val jobId: String?,
                     val options: Map<String, String>?,
                     val template: String?)