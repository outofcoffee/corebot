package com.gatehill.corebot.driver.rundeck.model

/**
 * Trigger a build with given options.
 */
data class ExecutionOptions(val argString: String,
                            val logLevel: String = "INFO",
                            val asUser: String = "",
                            val filter: String = "")
