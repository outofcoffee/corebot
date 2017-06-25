package com.gatehill.corebot.chat.parser

import com.gatehill.corebot.chat.model.template.ActionTemplate

interface CommandParser {
    fun parse(config: ParserConfig, template: ActionTemplate, command: String): Boolean
}
