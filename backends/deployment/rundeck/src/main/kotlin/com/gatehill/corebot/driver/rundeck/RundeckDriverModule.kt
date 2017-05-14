package com.gatehill.corebot.driver.rundeck

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.driver.rundeck.action.ExecutionStatusService
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriverImpl
import com.gatehill.corebot.driver.rundeck.action.RundeckJobTriggerService
import com.google.inject.AbstractModule

class RundeckDriverModule : AbstractModule() {
    override fun configure() {
        bind(RundeckActionDriver::class.java).to(RundeckActionDriverImpl::class.java).asSingleton()
        bind(ExecutionStatusService::class.java).asSingleton()
        bind(RundeckJobTriggerService::class.java).asSingleton()
    }
}
