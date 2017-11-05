package com.gatehill.corebot.test

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.SystemConfig

/**
 * Test data provider.
 */
object TestMother {
    const val actionName = "example"
    const val testJobUuid = "0ae1f9ce-e3a2-4661-9aa4-ddb4c3319104"

    val actions = mapOf(actionName to ActionConfig("template", testJobUuid, actionName, emptyMap(), emptyList(), SystemConfig.DefaultsConfig.defaultDriver, false, true, false))
    val trigger = TriggerContext("channelId", "userId", "username", "100", null)
}
