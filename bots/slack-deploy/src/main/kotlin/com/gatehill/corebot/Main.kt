package com.gatehill.corebot

import com.gatehill.corebot.chat.ActionTemplateConverter
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.deploy.TriggerActionTemplateConverter
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.gatehill.corebot.store.DataStore
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.apache.logging.log4j.LogManager

fun main(args: Array<String>) {
    Bot.build(DeployBotModule(), SlackModule()).start()
}

private class DeployBotModule : AbstractModule() {
    private val logger = LogManager.getLogger(DeployBotModule::class.java)

    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionTemplateConverter::class.java).to(TriggerActionTemplateConverter::class.java).asSingleton()

        bindDataStore()

        // drivers
        install(JenkinsDriverModule())
        install(RundeckDriverModule())
    }

    private fun bindDataStore() {
        val dataStoreImplClass = Settings.dataStores.implementationClass
        logger.debug("Using data store implementation: $dataStoreImplClass")

        bind(DataStore::class.java).annotatedWith(Names.named("lockStore"))
                .to(dataStoreImplClass).asSingleton()
    }
}
