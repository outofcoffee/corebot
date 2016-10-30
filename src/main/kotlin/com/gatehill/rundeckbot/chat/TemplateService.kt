package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ConfigService

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService {
    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<ActionTemplate>)

    private val configService = ConfigService()

    fun fetchCandidates(): TemplateContext {
        val candidates: MutableList<ActionTemplate> = configService.loadActions().values
                .map(::TriggerJobTemplate).toMutableList()

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
}
