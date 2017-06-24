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
            addAll(filterSimpleTemplates(commandOnly, candidates.filter { null == it.templateRegex }))
            addAll(filterRegexTemplates(commandOnly, candidates.filterNot { null == it.templateRegex }))
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
    private fun filterSimpleTemplates(commandOnly: String, candidates: Collection<ActionTemplate>) =
            candidates.filter { candidate ->
                splitMessageParts(commandOnly).forEach { token ->
                    // push command elements into the candidate templates
                    if (!candidate.accept(token)) {
                        return@filter false
                    }
                }
                return@filter true

            }.filter { candidate ->
                // include only satisfied candidates
                candidate.tokens.isEmpty()
            }

    private fun splitMessageParts(message: String) =
            message.trim().split(messagePartRegex).filterNot(String::isBlank)

    /**
     * Filter candidates based on their `templateRegex` property.
     */
    private fun filterRegexTemplates(commandOnly: String, candidates: Collection<ActionTemplate>) =
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
