package com.gatehill.corebot.chat.parser

import com.gatehill.corebot.chat.model.template.ActionTemplate
import java.util.LinkedList
import java.util.Queue

/**
 * The regular expression to tokenise messages.
 */
private val messagePartRegex = "\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?".toRegex()

class StringParser : CommandParser {
    class StringParserConfig(val template: String,
                             usage: String?) : ParserConfig(usage)

    override fun parse(config: ParserConfig, template: ActionTemplate, command: String) =
            parseCommand(config as StringParserConfig, template, command)

    /**
     * Split the command into elements and return `true` if all were processed successfully.
     */
    private fun parseCommand(config: StringParserConfig, template: ActionTemplate, command: String): Boolean {
        val tokens = LinkedList(config.template.split("\\s".toRegex()))

        command.trim().split(messagePartRegex).filterNot(String::isBlank).forEach { element ->
            // fail as soon as an element is rejected
            if (!parseElement(template, tokens, element)) {
                return false
            }
        }

        // consider only fully satisfied templates
        return tokens.isEmpty()
    }

    /**
     * Parse a command element and return `true` if it was accepted.
     */
    private fun parseElement(template: ActionTemplate, tokens: Queue<String>, element: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val accepted = "\\{(.*)}".toRegex().matchEntire(token)?.let { match ->
            // option placeholder
            template.placeholderValues[match.groupValues[1]] = element
            true

        } ?: run {
            // syntactic sugar
            token.equals(element, ignoreCase = true)
        }

        return if (accepted && tokens.isEmpty()) template.onTemplateSatisfied() else accepted
    }
}
