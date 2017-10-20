package com.gatehill.corebot.test

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.SystemConfig

/**
 * Test data provider.
 */
object TestMother {
    const val actionName = "example"
    val actions = mapOf(actionName to ActionConfig("template", "jobId", actionName, emptyMap(), emptyList(), SystemConfig.DefaultsConfig.defaultDriver, false, true, false))
    val trigger = TriggerContext("channelId", "userId", "username", "100", null)
}
