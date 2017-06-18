package com.gatehill.corebot.backend.rundeck

import com.gatehill.corebot.backend.rundeck.action.RundeckActionDriver
import com.gatehill.corebot.driver.ActionDriverFactory
import javax.inject.Inject

class RundeckDriverBootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory) {
    init {
        actionDriverFactory.registerDriver("rundeck", RundeckActionDriver::class.java)
    }
}