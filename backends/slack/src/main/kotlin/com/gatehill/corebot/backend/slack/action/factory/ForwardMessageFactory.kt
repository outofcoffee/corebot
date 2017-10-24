package com.gatehill.corebot.backend.slack.action.factory

import com.gatehill.corebot.backend.slack.action.factory.ForwardMessageFactory.Companion.channelPlaceholder
import com.gatehill.corebot.backend.slack.action.factory.ForwardMessageFactory.Companion.messagePlaceholder
import com.gatehill.corebot.backend.slack.action.model.SlackOperationType
import com.gatehill.corebot.backend.slack.service.SlackOutboundMessageService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.PlainOperationFactory
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import javax.inject.Inject

/**
 * Forward a message to a channel.
 */
@Template("forwardMessage", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL,
        placeholderKeys = arrayOf(
                messagePlaceholder,
                channelPlaceholder
        ))
class ForwardMessageFactory @Inject constructor(private val slackOutboundMessageService: SlackOutboundMessageService) : PlainOperationFactory() {
    override val operationType: OperationType = SlackOperationType.FORWARD_MESSAGE

    private val message
        get() = placeholderValues[messagePlaceholder]

    private val channel
        get() = placeholderValues[channelPlaceholder]

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    override fun onSatisfied() = !message.isNullOrBlank() && !channel.isNullOrBlank()

    override fun beforePerform(trigger: TriggerContext) {
        slackOutboundMessageService.forward(trigger, message!!, channel!!)
    }

    companion object {
        const val messagePlaceholder = "message"
        const val channelPlaceholder = "channel"
    }
}
