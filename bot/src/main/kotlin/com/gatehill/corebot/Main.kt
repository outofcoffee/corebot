package com.gatehill.corebot

import com.gatehill.corebot.action.*
import com.gatehill.corebot.chat.*
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.rundeck.action.ExecutionStatusService
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriverImpl
import com.gatehill.corebot.driver.rundeck.action.TriggerJobService
import com.gatehill.corebot.security.AuthorisationService
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.google.inject.binder.ScopedBindingBuilder
import javax.inject.Inject

fun main(args: Array<String>) {
    createInjector().getInstance(Bot::class.java).bootstrap()
}

/**
 * Set up dependency injection.
 */
private fun createInjector(): Injector {
    fun ScopedBindingBuilder.asSingleton() = this.`in`(Singleton::class.java)

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
            bind(RundeckActionDriver::class.java).to(RundeckActionDriverImpl::class.java).asSingleton()
            bind(ExecutionStatusService::class.java).asSingleton()
            bind(TriggerJobService::class.java).asSingleton()
        }
    })
}

private class Bot @Inject constructor(private val templateService: TemplateService,
                                      private val chatService: ChatService) {

    fun bootstrap() {
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(LockActionTemplate::class.java)
        templateService.registerTemplate(UnlockActionTemplate::class.java)
        templateService.registerTemplate(EnableJobTemplate::class.java)
        templateService.registerTemplate(DisableJobTemplate::class.java)
        templateService.registerTemplate(StatusJobTemplate::class.java)

        chatService.listenForEvents()
    }
}
