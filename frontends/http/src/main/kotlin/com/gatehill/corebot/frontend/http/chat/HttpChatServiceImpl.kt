package com.gatehill.corebot.frontend.http.chat

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.frontend.http.config.ChatSettings
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router

/**
 * Handles HTTP conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class HttpChatServiceImpl : ChatService {
    private val vertx: Vertx by lazy { Vertx.vertx() }

    private val router: Router by lazy { Router.router(vertx) }

    private val server: HttpServer by lazy {
        vertx.createHttpServer(HttpServerOptions())
                .requestHandler({ router.accept(it) })
    }

    override fun listenForEvents() {
        server.listen(ChatSettings.port, ChatSettings.hostname)
    }

    override fun stopListening() {
        server.close()
    }
}
