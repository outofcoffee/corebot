package com.gatehill.corebot.bot

import com.gatehill.corebot.action.NoOpOperationFactoryConverter
import com.gatehill.corebot.action.OperationFactoryConverter
import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.items.ItemsDriverModule
import com.gatehill.corebot.frontend.http.HttpModule
import com.google.inject.AbstractModule
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    println("Warning: the HTTP bot is experimental.")

    val bot = Bot.build(BotModule(), HttpModule())
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
        bind(BotBootstrap::class.java).asEagerSingleton()
        bind(OperationFactoryConverter::class.java).to(NoOpOperationFactoryConverter::class.java).asSingleton()

        // drivers
        install(ItemsDriverModule())
    }
}
