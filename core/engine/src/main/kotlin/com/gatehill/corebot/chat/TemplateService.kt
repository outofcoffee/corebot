package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.google.inject.Injector
import org.apache.logging.log4j.LogManager
import java.util.regex.Matcher
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService @Inject constructor(private val injector: Injector,
                                          private val configService: ConfigService,
                                          private val sessionService: SessionService,
                                          private val actionTemplateConverter: ActionTemplateConverter) {

    private val logger = LogManager.getLogger(TemplateService::class.java)

    /**
     * The regular expression to tokenise messages.
     */
    private val messagePartRegex = "\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?".toRegex()

    /**
     * Unique set of templates.
     */
    private val actionTemplates = mutableSetOf<Class<out ActionTemplate>>()

    fun registerTemplate(template: Class<out ActionTemplate>) {
        actionTemplates.add(template)
    }

    /**
     * Find the templates that match the specified command.
     *
     * @param commandOnly - the command, excluding any initial bot reference
     */
    fun findSatisfiedTemplates(commandOnly: String): Collection<ActionTemplate> {
        val candidates = fetchCandidates()

        // include those satisfying templates
        return mutableSetOf<ActionTemplate>().apply {
            addAll(filterSimpleTemplates(candidates.filter { null == it.templateRegex }, commandOnly))
            addAll(filterRegexTemplates(candidates.filterNot { null == it.templateRegex }, commandOnly))
        }
    }

    /**
     * Returns a new `Set` of candidates.
     */
    private fun fetchCandidates(): Set<ActionTemplate> = mutableSetOf<ActionTemplate>().apply {
        addAll(actionTemplateConverter.convertConfigToTemplate(configService.actions().values))
        addAll(actionTemplates.map({ actionTemplate -> injector.getInstance(actionTemplate) }))
    }

    /**
     * Filter candidates based on their template (or `tokens` property).
     */
    private fun filterSimpleTemplates(candidates: Collection<ActionTemplate>, commandOnly: String) =
            candidates.filter { candidate -> parseCommand(candidate, commandOnly) }

    /**
     * Split the command into elements and return `true` if all were processed successfully.
     */
    private fun parseCommand(template: ActionTemplate, command: String): Boolean {
        command.trim().split(messagePartRegex).filterNot(String::isBlank).forEach { element ->
            // fail as soon as an element is rejected
            if (!parseElement(template, element)) {
                return false
            }
        }

        // consider only fully satisfied templates
        return template.tokens.isEmpty()
    }

    /**
     * Parse a command element and return `true` if it was accepted.
     */
    private fun parseElement(template: ActionTemplate, element: String): Boolean {
        if (template.tokens.size == 0) return false
        val token = template.tokens.poll()

        val accepted: Boolean

        val match = "\\{(.*)}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            accepted = token.equals(element, ignoreCase = true)

        } else {
            // option placeholder
            template.placeholderValues[match.groupValues[1]] = element
            accepted = true
        }

        return if (accepted && template.tokens.isEmpty()) template.onTemplateSatisfied() else accepted
    }

    /**
     * Filter candidates based on their `templateRegex` property.
     */
    private fun filterRegexTemplates(candidates: Collection<ActionTemplate>, commandOnly: String) =
            candidates.map { it to it.templateRegex!!.matcher(commandOnly) }
                    .filter { (_, matcher) -> matcher.matches() }
                    .map { (template, matcher) -> injectPlaceholderValues(template, matcher) }
                    .filter { it.onTemplateSatisfied() }

    /**
     * Fetch the placeholder names from the template, then populate the placeholder values.
     * Return the template.
     */
    private fun injectPlaceholderValues(template: ActionTemplate, matcher: Matcher): ActionTemplate {
        template.tokens.filter { "\\{(.*)}".toRegex().matches(it) }
                .map { it.substring(1, it.length - 1) }
                .map { it to matcher.group(it) }.toMap()
                .let { placeholderValues ->
                    logger.trace("Placeholder values for ${template.actionType}: $placeholderValues")
                    template.placeholderValues += placeholderValues
                }

        return template
    }

    fun usage() = StringBuilder().apply {
        val sortedCandidates = fetchCandidates().toMutableList().apply {
            sortBy { candidate -> candidate.tokens.joinToString(" ") }
        }

        val printTemplate: (ActionTemplate) -> Unit = { candidate ->
            val template = candidate.tokens.joinToString(" ")
            appendln(); append("_@${sessionService.botUsername} ${template}_")
        }

        val customActions = sortedCandidates.filter(ActionTemplate::showInUsage).filterNot(ActionTemplate::builtIn)
        if (customActions.isNotEmpty()) {
            append("*Custom actions*")
            customActions.forEach(printTemplate)
        }

        if (isNotEmpty()) {
            appendln(); appendln()
        }

        val builtInActions = sortedCandidates.filter(ActionTemplate::showInUsage).filter(ActionTemplate::builtIn)
        if (builtInActions.isNotEmpty()) {
            append("*Built-in actions*")
            builtInActions.forEach(printTemplate)
        }
    }
}
