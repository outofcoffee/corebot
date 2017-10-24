package com.gatehill.corebot.backend.slack.action.model

import com.gatehill.corebot.operation.model.CoreOperationType

class SlackOperationType(name: String, description: String) : CoreOperationType(name, description) {
    companion object {
        val FORWARD_MESSAGE = SlackOperationType("FORWARD_MESSAGE", "Forward a message")
    }
}
