package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.action.*
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.JobConfig
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService {
    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<com.gatehill.rundeckbot.action.ActionTemplate>)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val configService = ConfigService()
    private val templatedJobs: MutableList<JobConfig> = ArrayList()

    constructor() {
        configService.loadJobs().values.forEach { job ->
            val template = job.template
            if (null != template) {
                templatedJobs.add(job)
            }
        }
        logger.debug("Loaded ${templatedJobs.size} templated jobs")
    }

    fun fetchCandidates(): TemplateContext {
        val candidates: MutableList<ActionTemplate> = templatedJobs.map(::TriggerActionTemplate).toMutableList()

        candidates.add(LockActionTemplate())
        candidates.add(UnlockActionTemplate())
        candidates.add(EnableActionTemplate())
        candidates.add(DisableActionTemplate())
        candidates.add(StatusActionTemplate())

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
