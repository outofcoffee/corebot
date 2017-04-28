package com.gatehill.corebot.driver.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the response to querying job output
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionOutput(val entries: List<ExecutionOutputEntry>)