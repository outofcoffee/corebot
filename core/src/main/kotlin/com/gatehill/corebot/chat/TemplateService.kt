package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.model.template.TriggerJobTemplate
import com.gatehill.corebot.config.ConfigService
import com.google.inject.Injector
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService @Inject constructor(private val injector: Injector,
                                          private val configService: ConfigService,
                                          private val sessionService: SessionService) {
    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<ActionTemplate>)

    private val actionTemplates = mutableListOf<Class<out ActionTemplate>>()

    fun registerTemplate(template: Class<out ActionTemplate>) {
        actionTemplates.add(template)
    }

    fun fetchCandidates(): TemplateContext {
        val candidates: MutableList<ActionTemplate> = configService.actions().values
                .map(::TriggerJobTemplate).toMutableList()

        candidates.addAll(actionTemplates.map({ actionTemplate -> injector.getInstance(actionTemplate) }))

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
        candidates.sortBy { candidate -> candidate.tokens.joinToString(" ") }

        val printTemplate: (ActionTemplate) -> Unit = { candidate ->
            val template = candidate.tokens.joinToString(" ")
            usage.appendln(); usage.append("_@${sessionService.botUsername} ${template}_")
        }

        val customActions = candidates.filter(ActionTemplate::showInUsage).filterNot(ActionTemplate::builtIn)
        if (customActions.isNotEmpty()) {
            usage.append("*Custom actions*")
            customActions.forEach(printTemplate)
        }

        if (usage.isNotEmpty()) {
            usage.appendln(); usage.appendln()
        }

        usage.append("*Built-in actions*")
        candidates.filter(ActionTemplate::showInUsage).filter(ActionTemplate::builtIn).forEach(printTemplate)
        return usage
    }
}
