package com.gatehill.corebot.bot

import com.gatehill.corebot.operation.NoOpOperationFactoryConverter
import com.gatehill.corebot.operation.OperationFactoryConverter
import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.items.ItemsDriverModule
import com.gatehill.corebot.frontend.websocket.WebSocketModule
import com.gatehill.corebot.frontend.websocket.chat.endpoint.CustomConfigurator
import com.gatehill.corebot.util.VersionUtil
import com.google.inject.AbstractModule

fun main(args: Array<String>) {
    println("Starting Corebot [version ${VersionUtil.version}]")
    println("Warning: the WebSocket bot is experimental.")
    Bot.build(BotModule(), WebSocketModule()).start()
}

private class BotModule : AbstractModule() {
    override fun configure() {
        requestStaticInjection(CustomConfigurator::class.java)

        bind(BotBootstrap::class.java).asEagerSingleton()
        bind(OperationFactoryConverter::class.java).to(NoOpOperationFactoryConverter::class.java).asSingleton()

        // drivers
        install(ItemsDriverModule())
    }
}
