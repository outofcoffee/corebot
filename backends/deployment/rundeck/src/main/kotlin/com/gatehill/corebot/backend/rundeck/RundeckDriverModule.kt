package com.gatehill.corebot.backend.rundeck

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.backend.rundeck.action.ExecutionStatusService
import com.gatehill.corebot.backend.rundeck.action.RundeckActionDriver
import com.gatehill.corebot.backend.rundeck.action.RundeckActionDriverImpl
import com.gatehill.corebot.backend.rundeck.action.RundeckJobTriggerService
import com.google.inject.AbstractModule

class RundeckDriverModule : AbstractModule() {
    override fun configure() {
        bind(RundeckDriverBootstrap::class.java).asEagerSingleton()
        bind(RundeckActionDriver::class.java).to(RundeckActionDriverImpl::class.java).asSingleton()
        bind(ExecutionStatusService::class.java).asSingleton()
        bind(RundeckJobTriggerService::class.java).asSingleton()
    }
}
