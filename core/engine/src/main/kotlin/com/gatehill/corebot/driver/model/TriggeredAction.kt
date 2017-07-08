package com.gatehill.corebot.driver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models a triggered action.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TriggeredAction(val id: Int,
                           val url: String,
                           val status: ActionStatus)
