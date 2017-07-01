package com.gatehill.corebot.chat.template

import com.gatehill.corebot.action.ActionFactoryConverter
import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.chat.filter.CommandFilter
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.chat.filter.RegexFilter
import com.gatehill.corebot.chat.filter.StringFilter
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
                                          private val actionFactoryConverter: ActionFactoryConverter,
                                          private val templateConfigService: TemplateConfigService) {

    private val logger = LogManager.getLogger(TemplateService::class.java)

    /**
     * Unique set of factories.
     */
    private val actionFactories = mutableSetOf<Class<out ActionFactory>>()

    fun registerTemplate(factory: Class<out ActionFactory>) {
        actionFactories += factory
    }

    /**
     * Find the templates that match the specified command.
     *
     * @param commandOnly - the command, excluding any initial bot reference
     */
    fun findSatisfiedTemplates(commandOnly: String): Collection<ActionFactory> =
            fetchCandidates().filter { factory -> factory.parsers.any { filterMatch(it, factory, commandOnly) } }.toSet()

    /**
     * Invoke the filter's match function for the given factory.
     */
    private fun filterMatch(config: FilterConfig, factory: ActionFactory, commandOnly: String) =
            loadFilter(config).matches(config, factory, templateConfigService.readMetadata(factory::class.java), commandOnly)

    /**
     * Load the filter for the given configuration.
     */
    private fun loadFilter(config: FilterConfig): CommandFilter = when (config) {
        is StringFilter.StringFilterConfig -> injector.getInstance(StringFilter::class.java)
        is RegexFilter.RegexFilterConfig -> injector.getInstance(RegexFilter::class.java)
        else -> throw UnsupportedOperationException("Unsupported filter config: ${config::class.java.canonicalName}")
    }

    /**
     * Return a new `Set` of candidates.
     */
    private fun fetchCandidates(): Set<ActionFactory> = mutableSetOf<ActionFactory>().apply {
        addAll(actionFactoryConverter.convertConfigToFactory(configService.actions().values))
        addAll(actionFactories.map({ actionTemplate -> injector.getInstance(actionTemplate) }))

        // populate the filter configurations
        forEach { template ->
            template.parsers += templateConfigService.loadFilterConfig(template::class.java)

            // no parsers have been set
            if (template.parsers.isEmpty()) {
                logger.warn("No filter configuration found for template: ${template::class.java.simpleName} - action ${template.actionType.name} cannot be invoked")
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

        val printTemplate: (ActionFactory) -> Unit = { candidate ->
            appendln()
            append(candidate.parsers
                    .filter { it.usage != null }
                    .map { "_@${sessionService.botUsername} ${it.usage}_" }
                    .joinToString("\n"))
        }

        val customActions = sortedCandidates.filter(ActionFactory::showInUsage).filterNot(ActionFactory::builtIn)
        if (customActions.isNotEmpty()) {
            append("*Custom actions*")
            customActions.forEach(printTemplate)
        }

        if (isNotEmpty()) repeat(2) { appendln() }

        val builtInActions = sortedCandidates.filter(ActionFactory::showInUsage).filter(ActionFactory::builtIn)
        if (builtInActions.isNotEmpty()) {
            append("*Built-in actions*")
            builtInActions.forEach(printTemplate)
        }
    }
}
