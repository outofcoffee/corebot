package com.gatehill.corebot

import com.gatehill.corebot.chat.ChatService
import com.google.inject.AbstractModule
import com.google.inject.Guice.createInjector
import com.google.inject.Module
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bot @Inject constructor(private val chatService: ChatService) {
    /**
     * The main entrypoint.
     */
    fun start() {
        chatService.listenForEvents()
    }

    fun stop() {
        chatService.stopListening()
    }

    /**
     * Constructs `Bot` instances.
     */
    companion object Builder {
        private val logger: Logger = LogManager.getLogger(Builder::class.java)

        /**
         * Construct a new `Bot` and wire up its dependencies.
         */
        fun build(vararg extensionModules: Module): Bot {
            val injector = createInjector(CoreModule(), CommonBotModule(), object : AbstractModule() {
                override fun configure() {
                    extensionModules.forEach {
                        logger.debug("Installing injection module: ${it.javaClass.canonicalName}")
                        install(it)
                    }
                }
            })
            return injector.getInstance(Bot::class.java)
        }
    }
}
