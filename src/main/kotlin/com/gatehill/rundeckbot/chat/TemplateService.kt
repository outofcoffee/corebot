package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ActionConfig
import com.gatehill.rundeckbot.config.ConfigService
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService {
    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<ActionTemplate>)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val configService = ConfigService()
    private val templatedActions: MutableList<ActionConfig> = ArrayList()

    constructor() {
        configService.loadActions().values.forEach { action ->
            val template = action.template
            if (null != template) {
                templatedActions.add(action)
            }
        }
        logger.debug("Loaded ${templatedActions.size} templated actions")
    }

    fun fetchCandidates(): TemplateContext {
        val candidates: MutableList<ActionTemplate> = templatedActions.map(::TriggerJobTemplate).toMutableList()

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
