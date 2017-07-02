package com.gatehill.corebot

import com.gatehill.corebot.action.ActionFactoryConverter
import com.gatehill.corebot.action.NoOpActionFactoryConverter
import com.gatehill.corebot.chat.endpoint.CustomConfigurator
import com.gatehill.corebot.driver.items.ItemsDriverModule
import com.gatehill.corebot.store.DataStoreModule
import com.google.inject.AbstractModule
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    val bot = Bot.build(ItemsBotModule(), WebSocketModule())
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

private class ItemsBotModule : AbstractModule() {
    override fun configure() {
        requestStaticInjection(CustomConfigurator::class.java)

        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionFactoryConverter::class.java).to(NoOpActionFactoryConverter::class.java).asSingleton()

        // data stores
        install(DataStoreModule("itemStore"))

        // drivers
        install(ItemsDriverModule())
    }
}
