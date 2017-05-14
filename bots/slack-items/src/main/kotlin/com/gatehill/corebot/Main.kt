package com.gatehill.corebot

import com.gatehill.corebot.chat.ActionTemplateConverter
import com.gatehill.corebot.chat.NoOpActionTemplateConverter
import com.gatehill.corebot.driver.items.ItemsDriverModule
import com.google.inject.AbstractModule

fun main(args: Array<String>) {
    Bot.build(ItemsBotModule(), SlackModule()).start()
}

private class ItemsBotModule : AbstractModule() {
    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionTemplateConverter::class.java).to(NoOpActionTemplateConverter::class.java).asSingleton()

        // drivers
        install(ItemsDriverModule())
    }
}
