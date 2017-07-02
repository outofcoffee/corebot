package com.gatehill.corebot.test

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Test data provider.
 */
object TestMother {
    const val actionName = "example"
    val actions = mapOf(actionName to ActionConfig("template", "jobId", actionName, null, null, null, null, null, null))
    val trigger = TriggerContext("channelId", "userId", "username", "100", null)
}
