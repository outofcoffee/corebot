package com.gatehill.corebot.backend.slack

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.slack.service.SlackOutboundMessageService
import com.google.inject.AbstractModule

class SlackDriverModule : AbstractModule() {
    override fun configure() {
        bind(SlackDriverBootstrap::class.java).asEagerSingleton()

        // services
        bind(SlackOutboundMessageService::class.java).asSingleton()
    }
}
