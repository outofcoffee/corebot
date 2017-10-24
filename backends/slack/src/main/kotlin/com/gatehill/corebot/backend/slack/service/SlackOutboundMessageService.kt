package com.gatehill.corebot.backend.slack.service

import com.gatehill.corebot.backend.slack.config.SlackSettings
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.ConfigService
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService @Inject constructor(private val configService: ConfigService,
                                                      private val sessionService: SessionService) {

    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    fun forward(channelName: String, message: String) {
        ensureChannelExists(channelName)

        val session = SlackSessionFactory.createWebSocketSlackSession(SlackSettings.authToken)
//        session.addSlackConnectedListener({ slackConnected: SlackConnected, slackSession: SlackSession ->
//
//        })
        session.connect()

        val channel = session.channels.firstOrNull { it.name == channelName } ?: run {
            throw IllegalStateException("Channel $channelName does not exist")
        }

        checkParticipants(session, channel)

        logger.info("Forwarding message to channel: $channelName: $message")
        session.sendMessage(channel, message)
    }

    private fun ensureChannelExists(channelName: String) {
        val session = SlackSessionFactory.createWebSocketSlackSession(SlackSettings.authToken)
//        session.addSlackConnectedListener({ slackConnected: SlackConnected, slackSession: SlackSession ->
//
//        })
        session.connect()

        try {
            session.channels.firstOrNull { it.name == channelName }?.let {
                // channel already exists
                logger.debug("Channel $channelName already exists")

            } ?: run {
                // create the channel
                logger.debug("Channel $channelName does not exist - creating")
                val params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                )
                val createChannelResponse = session.postGenericSlackCommand(params, "groups.create")
                logger.debug("Create channel response: $createChannelResponse")

                // if (createChannelResponse.waitForReply()
                session.channels
            }

        } finally {
            session.disconnect()
        }
    }

    private fun checkParticipants(session: SlackSession, channel: SlackChannel) {
        logger.debug("Checking partipants of channel: ${channel.name}")
        val channelUsernames = channel.members.map { it.userName }

        SlackSettings.members
                .filterNot { memberUsername -> channelUsernames.contains(memberUsername) }
                .forEach { memberUsername ->
                    logger.info("Inviting member $memberUsername to channel ${channel.name}")
                    session.inviteToChannel(channel, session.findUserByUserName(memberUsername))
                }
    }
}
