package com.gatehill.corebot.action.model

/**
 * Describes the context in which an action was triggered.
 */
data class TriggerContext(val channelId: String,
                          val userId: String,
                          val username: String,
                          val messageTimestamp: String,
                          val threadTimestamp: String)
