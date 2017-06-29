package com.gatehill.corebot.chat.template

import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.parser.CommandParser
import com.gatehill.corebot.chat.parser.ParserConfig
import com.gatehill.corebot.chat.parser.RegexParser
import com.gatehill.corebot.chat.parser.StringParser
import com.gatehill.corebot.config.ConfigService
import com.google.inject.Injector
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService @Inject constructor(private val injector: Injector,
                                          private val configService: ConfigService,
                                          private val sessionService: SessionService,
                                          private val actionTemplateConverter: ActionTemplateConverter,
                                          private val templateConfigService: TemplateConfigService) {

    private val logger = LogManager.getLogger(TemplateService::class.java)

    /**
     * Unique set of templates.
     */
    private val actionTemplates = mutableSetOf<Class<out ActionTemplate>>()

    fun registerTemplate(template: Class<out ActionTemplate>) {
        actionTemplates += template
    }

    /**
     * Find the templates that match the specified command.
     *
     * @param commandOnly - the command, excluding any initial bot reference
     */
    fun findSatisfiedTemplates(commandOnly: String): Collection<ActionTemplate> = fetchCandidates()
            .filter { template -> template.parsers.any { loadParser(it).parse(it, template, commandOnly) } }
            .toSet()

    /**
     * Load the `CommandParser` strategy for the given configuration.
     */
    private fun loadParser(parserConfig: ParserConfig): CommandParser = when (parserConfig) {
        is StringParser.StringParserConfig -> injector.getInstance(StringParser::class.java)
        is RegexParser.RegexParserConfig -> injector.getInstance(RegexParser::class.java)
        else -> throw UnsupportedOperationException("Unsupported parser config: ${parserConfig::class.java.canonicalName}")
    }

    /**
     * Return a new `Set` of candidates.
     */
    private fun fetchCandidates(): Set<ActionTemplate> = mutableSetOf<ActionTemplate>().apply {
        addAll(actionTemplateConverter.convertConfigToTemplate(configService.actions().values))
        addAll(actionTemplates.map({ actionTemplate -> injector.getInstance(actionTemplate) }))

        // populate the parser configurations
        forEach { template ->
            template.parsers += templateConfigService.loadParserConfig(template::class.java)

            // no parsers have been set
            if (template.parsers.isEmpty()) {
                logger.warn("No parser configuration found for template: ${template::class.java.simpleName} - action ${template.actionType.name} cannot be invoked")
            }
        }
    }

    /**
     * Provide a human-readable usage message.
     */
    fun usage() = StringBuilder().apply {
        val sortedCandidates = fetchCandidates().toMutableList().apply {
            sortBy { candidate ->
                candidate.parsers
                        .filter { it.usage != null }
                        .map { it.usage }
                        .joinToString("\n")
            }
        }

        val printTemplate: (ActionTemplate) -> Unit = { candidate ->
            appendln()
            append(candidate.parsers
                    .filter { it.usage != null }
                    .map { "_@${sessionService.botUsername} ${it.usage}_" }
                    .joinToString("\n"))
        }

        val customActions = sortedCandidates.filter(ActionTemplate::showInUsage).filterNot(ActionTemplate::builtIn)
        if (customActions.isNotEmpty()) {
            append("*Custom actions*")
            customActions.forEach(printTemplate)
        }

        if (isNotEmpty()) repeat(2) { appendln() }

        val builtInActions = sortedCandidates.filter(ActionTemplate::showInUsage).filter(ActionTemplate::builtIn)
        if (builtInActions.isNotEmpty()) {
            append("*Built-in actions*")
            builtInActions.forEach(printTemplate)
        }
    }
}
