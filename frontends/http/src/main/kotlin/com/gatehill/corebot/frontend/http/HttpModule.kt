package com.gatehill.corebot.frontend.http

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.frontend.http.chat.HttpChatServiceImpl
import com.gatehill.corebot.frontend.http.chat.HttpSessionService
import com.gatehill.corebot.frontend.http.chat.HttpSessionServiceImpl
import com.gatehill.corebot.frontend.session.chat.StatefulSessionService
import com.google.inject.AbstractModule

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpModule : AbstractModule() {
    override fun configure() {
        bind(ChatService::class.java).to(HttpChatServiceImpl::class.java).asSingleton()
        bind(SessionService::class.java).to(HttpSessionService::class.java)
        bind(StatefulSessionService::class.java).to(HttpSessionService::class.java)
        bind(HttpSessionService::class.java).to(HttpSessionServiceImpl::class.java).asSingleton()
    }
}
