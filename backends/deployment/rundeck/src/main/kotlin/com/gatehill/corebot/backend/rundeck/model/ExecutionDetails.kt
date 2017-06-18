package com.gatehill.corebot.backend.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the response to triggering a job.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionDetails(val id: Int,
                            val permalink: String,
                            val status: String)
