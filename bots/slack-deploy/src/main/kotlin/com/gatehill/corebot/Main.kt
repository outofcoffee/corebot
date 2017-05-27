package com.gatehill.corebot

import com.gatehill.corebot.chat.ActionTemplateConverter
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.deploy.TriggerActionTemplateConverter
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.gatehill.corebot.store.DataStore
import com.google.inject.AbstractModule
import com.google.inject.name.Names

fun main(args: Array<String>) {
    Bot.build(DeployBotModule(), SlackModule()).start()
}

private class DeployBotModule : AbstractModule() {
    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionTemplateConverter::class.java).to(TriggerActionTemplateConverter::class.java).asSingleton()

        bind(DataStore::class.java).annotatedWith(Names.named("lockStore"))
                .to(Settings.dataStores.implementationClass).asSingleton()

        // drivers
        install(JenkinsDriverModule())
        install(RundeckDriverModule())
    }
}
