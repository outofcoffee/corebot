package com.gatehill.corebot.chat.filter

import com.gatehill.corebot.operation.factory.OperationFactory
import java.util.LinkedList
import java.util.Queue

/**
 * The regular expression to tokenise messages.
 */
private val messagePartRegex = "\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?".toRegex()
private val tokenPartRegex = "\\s+(?![^{]*})".toRegex()
private val placeholderRegex = "\\{(.*)}".toRegex()

class StringFilter : CommandFilter {
    class StringFilterConfig(val template: String,
                             usage: String?) : FilterConfig(usage)

    override fun matches(config: FilterConfig, factory: OperationFactory, command: String) =
            parseCommand(config as StringFilterConfig, factory, command)

    /**
     * Split the command into elements and return `true` if all were processed successfully.
     */
    private fun parseCommand(config: StringFilterConfig, factory: OperationFactory, command: String): Boolean {
        val tokens = LinkedList(config.template.split(tokenPartRegex))

        command.trim().split(messagePartRegex).filterNot(String::isBlank).forEach { element ->
            // fail as soon as an element is rejected
            if (!parseElement(factory, tokens, element)) {
                return false
            }
        }

        // consider only fully satisfied templates
        return tokens.isEmpty()
    }

    /**
     * Parse a command element and return `true` if it was accepted.
     */
    private fun parseElement(factory: OperationFactory, tokens: Queue<String>, element: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val accepted = placeholderRegex.matchEntire(token)?.let { match ->
            // option placeholder
            factory.placeholderValues[match.groupValues[1]] = element
            true

        } ?: run {
            // syntactic sugar
            token.equals(element, ignoreCase = true)
        }

        return if (accepted && tokens.isEmpty()) factory.onSatisfied() else accepted
    }
}
