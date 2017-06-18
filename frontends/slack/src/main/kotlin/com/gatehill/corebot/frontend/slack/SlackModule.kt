package com.gatehill.corebot.frontend.slack

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.frontend.slack.chat.SlackChatServiceImpl
import com.gatehill.corebot.frontend.slack.chat.SlackSessionService
import com.gatehill.corebot.frontend.slack.chat.SlackSessionServiceImpl
import com.google.inject.AbstractModule

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackModule : AbstractModule() {
    override fun configure() {
        // chat
        bind(SessionService::class.java).to(SlackSessionService::class.java)
        bind(SlackSessionService::class.java).to(SlackSessionServiceImpl::class.java).asSingleton()
        bind(ChatService::class.java).to(SlackChatServiceImpl::class.java).asSingleton()
    }
}
