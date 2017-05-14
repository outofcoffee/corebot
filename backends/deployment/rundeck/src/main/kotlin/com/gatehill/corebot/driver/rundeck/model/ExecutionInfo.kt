package com.gatehill.corebot.driver.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models a triggered job execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionInfo(val status: String)
