package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.action.factory.Template

interface CommandFilter {
    fun matches(config: FilterConfig, factory: ActionFactory, template: Template, command: String): Boolean
}
