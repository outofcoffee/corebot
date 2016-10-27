package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ConfigService
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService {
    /**
     * A candidate job template.
     */
    class TaskTemplate {
        val job: ConfigService.JobConfig
        val placeholderValues = HashMap<String, String>()
        val tokens: Queue<String>

        constructor(job: ConfigService.JobConfig) {
            this.job = job
            tokens = LinkedList(job.template!!.split("\\s".toRegex()))
        }

        fun accept(input: String): Boolean {
            if (tokens.size == 0) return false
            val token = tokens.poll()

            val match = "\\{(.*)\\}".toRegex().matchEntire(token)
            if (null == match) {
                // syntactic sugar
                return (token == input)

            } else {
                // option placeholder
                placeholderValues[match.groupValues[1]] = input
                return true
            }
        }
    }

    /**
     * Holds candidate templates.
     */
    data class TemplateContext(var candidates: MutableList<TaskTemplate>)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val configService = ConfigService()
    private val templatedJobs: MutableList<ConfigService.JobConfig> = ArrayList()

    constructor() {
        configService.loadJobs().forEach { job ->
            val template = job.value.template
            if (null != template) {
                templatedJobs.add(job.value)
            }
        }
        logger.debug("Loaded ${templatedJobs.size} templated jobs")
    }

    fun fetchCandidates(): TemplateContext {
        return TemplateContext(templatedJobs.map(::TaskTemplate).toMutableList())
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
