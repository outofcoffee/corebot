package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.action.factory.OperationFactory

interface CommandFilter {
    fun matches(config: FilterConfig, factory: OperationFactory, command: String): Boolean
}
