package com.gatehill.corebot.backend.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models the individual response to querying job output
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionOutputEntry(val log: String)