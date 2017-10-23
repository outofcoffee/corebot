package com.gatehill.corebot.frontend.http.chat

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.MessageService
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.frontend.http.config.ChatSettings
import com.gatehill.corebot.operation.model.TriggerContext
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.time.Instant
import javax.inject.Inject

/**
 * Handles HTTP conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class HttpChatServiceImpl @Inject constructor(private val factoryService: FactoryService,
                                                   private val sessionService: HttpSessionService,
                                                   private val messageService: MessageService) : ChatService {

    private val vertx: Vertx by lazy { Vertx.vertx() }

    private val router: Router by lazy {
        Router.router(vertx).apply {
            route().handler { routingContext ->
                vertx.executeBlocking(
                        Handler<Future<Unit>> { handle(routingContext) },
                        Handler<AsyncResult<Unit>> { /**/ }
                )
            }
        }
    }

    private val server: HttpServer by lazy {
        vertx.createHttpServer(HttpServerOptions().setPort(ChatSettings.port).setHost(ChatSettings.hostname))
                .requestHandler(router::accept)
    }

    private fun handle(routingContext: RoutingContext) {
        val operationFactory = factoryService.allFactories.firstOrNull { factory ->
            factory.readMetadata().templateName == routingContext.normalisedPath().substring(1)
        }

        operationFactory?.let { factory ->
            val sessionHolder = HttpSessionHolder(
                    session = routingContext
            )
            sessionService.connectedSessions += sessionHolder

            try {
                factory.readMetadata().placeholderKeys.forEach { key ->
                    routingContext.request().params()[key]?.let { placeholderValue ->
                        factory.placeholderValues[key] = placeholderValue
                    }
                }

                val trigger = TriggerContext(
                        channelId = sessionHolder.sessionId,
                        userId = sessionHolder.username,
                        username = sessionHolder.realName,
                        messageTimestamp = Instant.now().toEpochMilli().toString(),
                        messageThreadTimestamp = null
                )

                val operationContext = messageService.buildOperationContext(trigger, factory)
                messageService.handleCommand(trigger, operationContext)

            } catch (e: Exception) {
                routingContext.fail(e)
            } finally {
                sessionService.connectedSessions -= sessionHolder
            }

        } ?: routingContext.response().apply {
            statusCode = 404
            end()
        }
    }

    override fun listenForEvents() {
        server.listen()
    }

    override fun stopListening() {
        server.close()
    }
}
