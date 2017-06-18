package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.operation.factory.OperationFactory
import java.util.regex.Pattern

class RegexFilter : CommandFilter {
    class RegexFilterConfig(val template: Pattern,
                            usage: String?) : FilterConfig(usage)

    override fun matches(config: FilterConfig, factory: OperationFactory, command: String): Boolean =
            parseCommand(config as RegexFilterConfig, factory, command)

    /**
     * Filter candidates based on their templates.
     */
    private fun parseCommand(config: RegexFilterConfig, factory: OperationFactory, command: String) =
            config.template.matcher(command).takeIf { it.matches() }?.let { matcher ->
                factory.placeholderValues += factory.readMetadata().placeholderKeys.map { it to matcher.group(it) }.toMap()
                factory.onSatisfied()
            } ?: false
}
