package com.gatehill.corebot.backend.jenkins

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.jenkins.action.JenkinsActionDriver
import com.gatehill.corebot.backend.jenkins.action.JenkinsActionDriverImpl
import com.gatehill.corebot.backend.jenkins.action.JenkinsJobTriggerService
import com.google.inject.AbstractModule

class JenkinsDriverModule : AbstractModule() {
    override fun configure() {
        bind(JenkinsDriverBootstrap::class.java).asEagerSingleton()
        bind(JenkinsActionDriver::class.java).to(JenkinsActionDriverImpl::class.java).asSingleton()
        bind(JenkinsJobTriggerService::class.java).asSingleton()
    }
}
