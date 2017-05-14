package com.gatehill.corebot

import com.gatehill.corebot.chat.ActionTemplateConverter
import com.gatehill.corebot.deploy.TriggerActionTemplateConverter
import com.gatehill.corebot.driver.jenkins.JenkinsDriverModule
import com.gatehill.corebot.driver.rundeck.RundeckDriverModule
import com.google.inject.AbstractModule

fun main(args: Array<String>) {
    Bot.build(DeployBotModule(), SlackModule()).start()
}

private class DeployBotModule : AbstractModule() {
    override fun configure() {
        bind(Bootstrap::class.java).asEagerSingleton()
        bind(ActionTemplateConverter::class.java).to(TriggerActionTemplateConverter::class.java).asSingleton()

        // drivers
        install(JenkinsDriverModule())
        install(RundeckDriverModule())
    }
}
