package com.gatehill.corebot

import com.gatehill.corebot.action.OperationFactoryConverter
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.jobs.action.TriggerOperationFactoryConverter
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.gatehill.corebot.store.DataStoreModule
import com.google.inject.AbstractModule

fun main(args: Array<String>) {
    Bot.build(DeployBotModule(), SlackModule()).start()
}

private class DeployBotModule : AbstractModule() {
    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(LockService::class.java).asSingleton()
        bind(OperationFactoryConverter::class.java).to(TriggerOperationFactoryConverter::class.java).asSingleton()

        // data stores
        install(DataStoreModule("lockStore"))

        // drivers
        install(JenkinsDriverModule())
        install(RundeckDriverModule())
    }
}
