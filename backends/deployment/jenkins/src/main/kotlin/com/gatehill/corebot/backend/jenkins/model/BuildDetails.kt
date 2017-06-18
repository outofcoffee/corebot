package com.gatehill.corebot.backend.jenkins.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the response to triggering a job.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildDetails(val url: String,
                        val number: Int,
                        val building: Boolean,
                        val result: String?)
