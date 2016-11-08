package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture

interface ActionDriver {
    fun perform(channelId: String, triggerMessageSenderId: String, triggerMessageTimestamp: String,
                actionType: ActionType, action: ActionConfig,
                args: Map<String, String>): CompletableFuture<PerformActionResult>
}
