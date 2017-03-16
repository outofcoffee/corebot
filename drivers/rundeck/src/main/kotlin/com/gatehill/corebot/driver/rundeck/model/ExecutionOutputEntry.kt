package com.gatehill.corebot.driver.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the response to triggering a job.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionOutputEntry(val log: String)
