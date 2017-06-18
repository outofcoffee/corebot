package com.gatehill.corebot.backend.jenkins.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models a queued item.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class QueuedItem(val executable: Executable?)

/**
 * Details of a queued item's execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Executable(val number: Int?)
