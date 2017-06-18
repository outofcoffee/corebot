package com.gatehill.corebot.frontend.websocket

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.frontend.websocket.chat.WebSocketChatServiceImpl
import com.gatehill.corebot.frontend.websocket.chat.WebSocketSessionService
import com.gatehill.corebot.frontend.websocket.chat.WebSocketSessionServiceImpl
import com.google.inject.AbstractModule

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class WebSocketModule : AbstractModule() {
    override fun configure() {
        bind(ChatService::class.java).to(WebSocketChatServiceImpl::class.java).asSingleton()
        bind(SessionService::class.java).to(WebSocketSessionService::class.java)
        bind(WebSocketSessionService::class.java).to(WebSocketSessionServiceImpl::class.java).asSingleton()
    }
}
