package com.gatehill.corebot

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.chat.SlackChatServiceImpl
import com.gatehill.corebot.chat.SlackSessionService
import com.gatehill.corebot.chat.SlackSessionServiceImpl
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
