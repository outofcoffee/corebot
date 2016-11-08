package com.gatehill.corebot.driver.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Information about a job execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionInfo(val status: String)
