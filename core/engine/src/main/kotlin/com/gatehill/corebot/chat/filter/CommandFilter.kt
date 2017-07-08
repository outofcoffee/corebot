package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.action.factory.ActionFactory

interface CommandFilter {
    fun matches(config: FilterConfig, factory: ActionFactory, command: String): Boolean
}
