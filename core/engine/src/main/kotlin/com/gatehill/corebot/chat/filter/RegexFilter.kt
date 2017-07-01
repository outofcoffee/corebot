package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.action.factory.Template
import java.util.regex.Pattern

class RegexFilter : CommandFilter {
    class RegexFilterConfig(val template: Pattern,
                            usage: String?) : FilterConfig(usage)

    override fun matches(config: FilterConfig, factory: ActionFactory, template: Template, command: String): Boolean =
            parseCommand(config as RegexFilterConfig, factory, template, command)

    /**
     * Filter candidates based on their templates.
     */
    private fun parseCommand(config: RegexFilterConfig, factory: ActionFactory, template: Template, command: String) =
            config.template.matcher(command).takeIf { it.matches() }?.let { matcher ->
                factory.placeholderValues += template.placeholderKeys.map { it to matcher.group(it) }.toMap()
                factory.onSatisfied()
            } ?: false
}
