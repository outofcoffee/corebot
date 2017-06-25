package com.gatehill.corebot.chat.parser

import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.model.template.RegexActionTemplate
import java.util.regex.Pattern

class RegexParser : CommandParser {
    class RegexParserConfig(val template: Pattern,
                            usage: String?) : ParserConfig(usage)

    override fun parse(config: ParserConfig, template: ActionTemplate, command: String): Boolean =
            parseCommand(config as RegexParserConfig, template as RegexActionTemplate, command)

    /**
     * Filter candidates based on their templates.
     */
    private fun parseCommand(config: RegexParserConfig, template: RegexActionTemplate, command: String) =
            config.template.matcher(command).takeIf { it.matches() }?.let { matcher ->
                template.placeholderValues += template.placeholderKeys.map { it to matcher.group(it) }.toMap()
                template.onTemplateSatisfied()
            } ?: false
}
