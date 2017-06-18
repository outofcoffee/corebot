package com.gatehill.corebot.chat.template

import com.gatehill.corebot.action.OperationFactoryConverter
import com.gatehill.corebot.operation.factory.OperationFactory
import com.gatehill.corebot.operation.factory.Template
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
class FactoryService @Inject constructor(private val injector: Injector,
                                         private val configService: ConfigService,
                                         private val sessionService: SessionService,
                                         private val operationFactoryConverter: OperationFactoryConverter,
                                         private val templateService: TemplateService) {

    private val logger = LogManager.getLogger(FactoryService::class.java)

    /**
     * Unique set of factories.
     */
    private val operationFactories = mutableSetOf<Class<out OperationFactory>>()

    fun registerFactory(factory: Class<out OperationFactory>) {
        operationFactories += factory
    }

    /**
     * Find the factories that match the specified command.
     *
     * @param command - the command, excluding any initial bot reference
     */
    fun findSatisfiedFactories(command: String): Collection<OperationFactory> =
            fetchCandidates().filter { factory -> factory.parsers.any { filterMatch(it, factory, command) } }.toSet()

    /**
     * Invoke the filter's match function for the given factory.
     */
    private fun filterMatch(config: FilterConfig, factory: OperationFactory, command: String) =
            loadFilter(config).matches(config, factory, command)

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
    private fun fetchCandidates(): Set<OperationFactory> = mutableSetOf<OperationFactory>().apply {
        addAll(operationFactoryConverter.convertConfigToFactory(configService.actions().values))
        addAll(operationFactories.map({ factory -> injector.getInstance(factory) }))

        // populate the filter configurations
        forEach { factory ->
            factory.parsers += templateService.loadFilterConfig(factory::class.java)

            // no parsers have been set
            if (factory.parsers.isEmpty()) {
                logger.warn("No filter configuration found for factory: ${factory::class.java.simpleName} - action ${factory.operationType.name} cannot be invoked")
            }
        }
    }

    /**
     * Provide a human-readable usage message.
     */
    fun usage() = StringBuilder().apply {
        val metadata = fetchCandidates()
                .toMutableList()
                .apply {
                    sortBy { candidate ->
                        candidate.parsers
                                .filter { it.usage != null }
                                .map { it.usage }
                                .joinToString("\n")
                    }
                }
                .map { it to it.readMetadata() }.toMap()

        // uses the local metadata map for each lookup
        fun OperationFactory.cachedMetadata(): Template = metadata[this]!!

        val printFactory: (OperationFactory) -> Unit = { candidate ->
            appendln()
            append(candidate.parsers
                    .filter { it.usage != null }
                    .map { "_@${sessionService.botUsername} ${it.usage}_" }
                    .joinToString("\n"))
        }

        val actionOperations = metadata.keys.filter { it.cachedMetadata().showInUsage }.filterNot { it.cachedMetadata().builtIn }
        if (actionOperations.isNotEmpty()) {
            append("*External actions*")
            actionOperations.forEach(printFactory)
        }

        if (isNotEmpty()) repeat(2) { appendln() }

        val plainOperations = metadata.keys.filter { it.cachedMetadata().showInUsage }.filter { it.cachedMetadata().builtIn }
        if (plainOperations.isNotEmpty()) {
            append("*Built-in operations*")
            plainOperations.forEach(printFactory)
        }
    }
}
