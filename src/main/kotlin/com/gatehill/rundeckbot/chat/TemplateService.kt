package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ConfigService

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object TemplateService {
    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<ActionTemplate>)

    private val configService by lazy { ConfigService }
    private val sessionService by lazy { SessionService }

    fun fetchCandidates(): TemplateContext {
        val candidates: MutableList<ActionTemplate> = configService.actions().values
                .map(::TriggerJobTemplate).toMutableList()

        candidates.add(ShowHelpTemplate())
        candidates.add(LockActionTemplate())
        candidates.add(UnlockActionTemplate())
        candidates.add(EnableJobTemplate())
        candidates.add(DisableJobTemplate())
        candidates.add(StatusJobTemplate())

        return TemplateContext(candidates)
    }

    fun process(context: TemplateContext, token: String) {
        // iterate over a copy to prevent concurrent modification issues
        context.candidates.toList().forEach { candidate ->
            if (!candidate.accept(token)) {
                context.candidates.remove(candidate)
            }
        }
    }

    fun usage(): StringBuilder {
        val usage = StringBuilder()
        val candidates = fetchCandidates().candidates

        val printTemplate: (ActionTemplate) -> Unit = { candidate ->
            val template = candidate.tokens.joinToString(" ")
            usage.appendln(); usage.append("_@${sessionService.botUsername} ${template}_")
        }

        val customActions = candidates.filter(ActionTemplate::showInUsage).filterNot(ActionTemplate::builtIn)
        if (customActions.size > 0) {
            usage.append("*Custom actions*")
            customActions.forEach(printTemplate)
        }

        if (usage.length > 0) {
            usage.appendln(); usage.appendln()
        }

        usage.append("*Built-in actions*")
        candidates.filter(ActionTemplate::showInUsage).filter(ActionTemplate::builtIn).forEach(printTemplate)
        return usage
    }
}
