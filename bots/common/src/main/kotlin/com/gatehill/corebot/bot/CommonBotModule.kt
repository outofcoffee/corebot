package com.gatehill.corebot.bot

import com.gatehill.corebot.action.ActionOutcomeService
import com.gatehill.corebot.action.ActionOutcomeServiceImpl
import com.gatehill.corebot.action.ActionPerformService
import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.bot.action.DirectActionPerformServiceImpl
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.ChatGeneratorImpl
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.ConfigServiceImpl
import com.gatehill.corebot.driver.ActionDriverFactory
import com.gatehill.corebot.security.AuthorisationService
import com.google.inject.AbstractModule

/**
 * Common bot bindings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class CommonBotModule : AbstractModule() {
    override fun configure() {
        // utility
        bind(ConfigService::class.java).to(ConfigServiceImpl::class.java).asSingleton()
        bind(AuthorisationService::class.java).asSingleton()

        // chat
        bind(FactoryService::class.java).asSingleton()
        bind(ChatGenerator::class.java).to(ChatGeneratorImpl::class.java).asSingleton()

        // actions
        bind(ActionPerformService::class.java).to(DirectActionPerformServiceImpl::class.java)
        bind(ActionDriverFactory::class.java).asSingleton()
        bind(ActionOutcomeService::class.java).to(ActionOutcomeServiceImpl::class.java)
    }
}
