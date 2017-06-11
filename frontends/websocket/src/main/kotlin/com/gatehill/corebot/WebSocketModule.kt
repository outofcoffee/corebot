package com.gatehill.corebot

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.chat.WebSocketChatServiceImpl
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.chat.WebSocketSessionServiceImpl
import com.google.inject.AbstractModule

/**
 *
 * @author pete
 */
class WebSocketModule : AbstractModule() {
    override fun configure() {
        bind(ChatService::class.java).to(WebSocketChatServiceImpl::class.java).asSingleton()
        bind(SessionService::class.java).to(WebSocketSessionService::class.java)
        bind(WebSocketSessionService::class.java).to(WebSocketSessionServiceImpl::class.java).asSingleton()
    }
}
