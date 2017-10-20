package com.gatehill.corebot.operation.model

/**
 * Describes the context in which an operation was triggered.
 */
data class TriggerContext(val channelId: String,
                          val userId: String,
                          val username: String,
                          val messageTimestamp: String,
                          val messageThreadTimestamp: String?)
