package com.gatehill.corebot.driver.rundeck.model

/**
 * Describes a job execution.
 */
data class ExecutionOptions(val argString: String,
                            val logLevel: String = "INFO",
                            val asUser: String = "",
                            val filter: String = "")
