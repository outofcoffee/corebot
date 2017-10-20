package com.gatehill.corebot.bot

import com.gatehill.corebot.action.NoOpOperationFactoryConverter
import com.gatehill.corebot.action.OperationFactoryConverter
import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.items.ItemsDriverModule
import com.gatehill.corebot.frontend.websocket.WebSocketModule
import com.gatehill.corebot.frontend.websocket.chat.endpoint.CustomConfigurator
import com.google.inject.AbstractModule
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    println("Warning: the websocket bot is experimental.")

    val bot = Bot.build(BotModule(), WebSocketModule())
    bot.start()

    try {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        println("Please press a key to stop the server.")
        reader.readLine()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        bot.stop()
    }
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
