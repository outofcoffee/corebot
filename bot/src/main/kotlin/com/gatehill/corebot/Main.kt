package com.gatehill.corebot

import com.gatehill.corebot.action.ActionDriverFactory
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.chat.*
import com.gatehill.corebot.chat.model.template.*
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.gatehill.corebot.security.AuthorisationService
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import javax.inject.Inject

fun main(args: Array<String>) {
    createInjector().getInstance(Bot::class.java).bootstrap()
}

/**
 * Set up dependency injection.
 */
private fun createInjector(): Injector {
    return Guice.createInjector(object : AbstractModule() {
        override fun configure() {
            // utility
            bind(ConfigService::class.java).asSingleton()
            bind(LockService::class.java).asSingleton()
            bind(AuthorisationService::class.java).asSingleton()

            // chat
            bind(SessionService::class.java).to(SlackSessionService::class.java)
            bind(SlackSessionService::class.java).to(SlackSessionServiceImpl::class.java).asSingleton()
            bind(ChatService::class.java).asSingleton()
            bind(TemplateService::class.java).asSingleton()

            // drivers
            bind(ActionDriverFactory::class.java).asSingleton()
            install(JenkinsDriverModule())
            install(RundeckDriverModule())
        }
    })
}

private class Bot @Inject constructor(private val templateService: TemplateService,
                                      private val chatService: ChatService) {

    fun bootstrap() {
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(LockActionTemplate::class.java)
        templateService.registerTemplate(UnlockActionTemplate::class.java)
        templateService.registerTemplate(StatusJobTemplate::class.java)
        templateService.registerTemplate(EnableJobTemplate::class.java)
        templateService.registerTemplate(DisableJobTemplate::class.java)
        templateService.registerTemplate(LockOptionTemplate::class.java)
        templateService.registerTemplate(UnlockOptionTemplate::class.java)

        chatService.listenForEvents()
    }
}
