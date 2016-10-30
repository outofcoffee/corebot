package com.gatehill.rundeckbot.action.model

/**
 * Trigger a build with given options.
 */
data class ExecutionOptions(val argString: String,
                            val logLevel: String = "INFO",
                            val asUser: String = "",
                            val filter: String = "")
