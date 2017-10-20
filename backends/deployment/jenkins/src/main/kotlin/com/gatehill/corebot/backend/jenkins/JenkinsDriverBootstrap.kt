package com.gatehill.corebot.backend.jenkins

import com.gatehill.corebot.backend.jenkins.action.JenkinsActionDriver
import com.gatehill.corebot.driver.ActionDriverFactory
import javax.inject.Inject

class JenkinsDriverBootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory) {
    init {
        actionDriverFactory.registerDriver("jenkins", JenkinsActionDriver::class.java)
    }
}
