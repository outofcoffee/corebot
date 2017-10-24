package com.gatehill.corebot.backend.slack.service

import com.gatehill.corebot.backend.slack.config.SlackSettings
import com.gatehill.corebot.backend.slack.model.GroupsCreateResponse
import com.gatehill.corebot.backend.slack.model.GroupsListResponse
import com.gatehill.corebot.backend.slack.model.SlackGroup
import com.gatehill.corebot.backend.slack.model.UsersListResponse
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.operation.model.TriggerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService @Inject constructor(private val slackApiService: SlackApiService,
                                                      private val sessionService: SessionService) {

    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    private val users by lazy {
        val reply = slackApiService.invokeSlackCommand<UsersListResponse>("users.list")
        slackApiService.checkReplyOk(reply.ok)
        reply.members
    }

    fun forward(trigger: TriggerContext, message: String, channelName: String) {
        val channel = ensureChannelExists(channelName)
        checkParticipants(channel)
        sendMessage(channelName, message)

        sessionService.sendMessage(trigger, "OK")
    }

    private fun sendMessage(channelName: String, message: String) {
        logger.info("Forwarding message to channel: $channelName: $message")

        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>("chat.postMessage", mapOf(
                "channel" to channelName,
                "text" to message
        ))
        slackApiService.checkReplyOk(reply)
    }

    private fun ensureChannelExists(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsListResponse>("groups.list")
        slackApiService.checkReplyOk(reply.ok)

        reply.groups.firstOrNull { it.name == channelName }?.let {
            // channel already exists
            logger.debug("Channel $channelName already exists")
            return it

        } ?: run {
            // create the channel
            logger.debug("Channel $channelName does not exist - creating")
            return createChannel(channelName)
        }
    }

    private fun createChannel(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsCreateResponse>("groups.create", mapOf(
                "name" to channelName,
                "validate" to "true"
        ))

        logger.debug("Create channel response: $reply")

        try {
            slackApiService.checkReplyOk(reply.ok)
            logger.debug("Channel $channelName created")
            return reply.group

        } catch (e: Exception) {
            throw RuntimeException("Error parsing channel creation response", e)
        }
    }

    private fun checkParticipants(channel: SlackGroup) {
        logger.debug("Checking participants of channel: ${channel.name}")

        val memberIds = SlackSettings.members.mapNotNull { memberUsername ->
            users.firstOrNull { user -> user.name == memberUsername }?.id
        }

        memberIds
                .filterNot { memberId: String -> channel.members.contains(memberId) }
                .forEach { memberId ->
                    logger.info("Inviting member $memberId to channel ${channel.name}")
                    inviteToChannel(channel, memberId)
                }
    }

    private fun inviteToChannel(channel: SlackGroup, memberId: String) {
        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>("groups.invite", mapOf(
                "channel" to channel.id,
                "user" to memberId
        ))
        slackApiService.checkReplyOk(reply)
    }
}
