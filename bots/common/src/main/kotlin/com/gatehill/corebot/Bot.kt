package com.gatehill.corebot

import com.gatehill.corebot.action.*
import com.gatehill.corebot.action.driver.ActionDriverFactory
import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.TemplateService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.ConfigServiceImpl
import com.gatehill.corebot.security.AuthorisationService
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

    /**
     * Constructs `Bot` instances.
     */
    companion object Builder {
        private val injectionModuleSystemProperty = "com.gatehill.corebot.InjectionModule"
        private val logger: Logger = LogManager.getLogger(Builder::class.java)

        /**
         * Construct a new `Bot` and wire up its dependencies.
         */
        fun build(vararg extensionModules: Module): Bot =
                createInjector(extensionModules).getInstance(Bot::class.java)

        /**
         * Set up dependency injection.
         */
        private fun createInjector(extensionModules: Array<out Module>) = createInjector(object : AbstractModule() {
            override fun configure() {
                // utility
                bind(ConfigService::class.java).to(ConfigServiceImpl::class.java).asSingleton()
                bind(LockService::class.java).asSingleton()
                bind(AuthorisationService::class.java).asSingleton()

                // chat
                bind(TemplateService::class.java).asSingleton()

                // actions
                bind(ActionPerformService::class.java).to(DirectActionPerformServiceImpl::class.java)
                bind(ActionDriverFactory::class.java).asSingleton()
                bind(ActionOutcomeService::class.java).to(ActionOutcomeServiceImpl::class.java)

                // extension point
                with(extensionModules.toMutableList()) {
                    System.getProperty(injectionModuleSystemProperty)?.let {
                        add(Class.forName(it).newInstance() as Module)
                    }
                    forEach {
                        logger.debug("Installing injection module: ${it.javaClass.canonicalName}")
                        install(it)
                    }
                }
            }
        })
    }
}
