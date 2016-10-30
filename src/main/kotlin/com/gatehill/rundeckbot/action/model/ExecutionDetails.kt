package com.gatehill.rundeckbot.action.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the response to triggering a build.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionDetails(val id: Int,
                            val permalink: String,
                            val status: String)
