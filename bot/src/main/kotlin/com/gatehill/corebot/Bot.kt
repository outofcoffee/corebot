package com.gatehill.corebot

import com.gatehill.corebot.action.*
import com.gatehill.corebot.action.driver.ActionDriver
import com.gatehill.corebot.action.driver.ActionDriverFactory
import com.gatehill.corebot.chat.*
import com.gatehill.corebot.chat.model.template.*
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.ConfigServiceImpl
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.jenkins.action.JenkinsActionDriver
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
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
class Bot @Inject constructor(actionDriverFactory: ActionDriverFactory,
                              templateService: TemplateService,
                              private val chatService: ChatService) {
    init {
        // drivers
        System.getProperty(driverFactorySystemProperty)?.let {
            val (driverName, driverClass) = it.split("=").let { factory -> Pair(factory[0], factory[1]) }
            logger.debug("Registering '$driverName' driver factory: $driverClass")

            @Suppress("UNCHECKED_CAST")
            actionDriverFactory.registerDriver(driverName, Class.forName(driverClass) as Class<out ActionDriver>)

        } ?: run {
            // default drivers
            logger.debug("Registering default driver factories")
            actionDriverFactory.registerDriver("rundeck", RundeckActionDriver::class.java)
            actionDriverFactory.registerDriver("jenkins", JenkinsActionDriver::class.java)
        }

        // built-in templates
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(LockActionTemplate::class.java)
        templateService.registerTemplate(UnlockActionTemplate::class.java)
        templateService.registerTemplate(StatusJobTemplate::class.java)
        templateService.registerTemplate(EnableJobTemplate::class.java)
        templateService.registerTemplate(DisableJobTemplate::class.java)
        templateService.registerTemplate(LockOptionTemplate::class.java)
        templateService.registerTemplate(UnlockOptionTemplate::class.java)
    }

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
        private val driverFactorySystemProperty = "com.gatehill.corebot.DriverFactory"
        private val driverModuleSystemProperty = "com.gatehill.corebot.DriverModule"
        private val injectionModuleSystemProperty = "com.gatehill.corebot.InjectionModule"
        private val logger: Logger = LogManager.getLogger(Builder::class.java)

        /**
         * Construct a new `Bot` and wire up its dependencies.
         */
        fun build(): Bot = createInjector().getInstance(Bot::class.java)

        /**
         * Set up dependency injection.
         */
        private fun createInjector() = createInjector(object : AbstractModule() {
            override fun configure() {
                // utility
                bind(ConfigService::class.java).to(ConfigServiceImpl::class.java).asSingleton()
                bind(LockService::class.java).asSingleton()
                bind(AuthorisationService::class.java).asSingleton()

                // chat
                bind(SessionService::class.java).to(SlackSessionService::class.java)
                bind(SlackSessionService::class.java).to(SlackSessionServiceImpl::class.java).asSingleton()
                bind(ChatService::class.java).to(SlackChatServiceImpl::class.java).asSingleton()
                bind(TemplateService::class.java).asSingleton()

                // actions
                bind(ActionPerformService::class.java).to(DirectActionPerformServiceImpl::class.java)
                bind(ActionDriverFactory::class.java).asSingleton()
                bind(ActionOutcomeService::class.java).to(ActionOutcomeServiceImpl::class.java)

                // drivers
                System.getProperty(driverModuleSystemProperty)?.let {
                    logger.debug("Installing driver module: $it")
                    install(Class.forName(it).newInstance() as Module)

                } ?: run {
                    // default drivers
                    logger.debug("Installing default driver modules")
                    install(JenkinsDriverModule())
                    install(RundeckDriverModule())
                }

                // generic extension point
                System.getProperty(injectionModuleSystemProperty)?.let {
                    logger.debug("Installing injection module: $it")
                    install(Class.forName(it).newInstance() as Module)
                }
            }
        })
    }
}
