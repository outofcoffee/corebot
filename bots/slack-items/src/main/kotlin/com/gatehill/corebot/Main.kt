package com.gatehill.corebot

import com.gatehill.corebot.chat.template.ActionTemplateConverter
import com.gatehill.corebot.chat.template.NoOpActionTemplateConverter
import com.gatehill.corebot.driver.items.ItemsDriverModule
import com.gatehill.corebot.store.DataStoreModule
import com.google.inject.AbstractModule

fun main(args: Array<String>) {
    Bot.build(ItemsBotModule(), SlackModule()).start()
}

private class ItemsBotModule : AbstractModule() {
    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionTemplateConverter::class.java).to(NoOpActionTemplateConverter::class.java).asSingleton()

        // data stores
        install(DataStoreModule("itemStore"))

        // drivers
        install(ItemsDriverModule())
    }
}
