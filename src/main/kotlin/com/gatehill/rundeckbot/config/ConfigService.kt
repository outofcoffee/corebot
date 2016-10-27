package com.gatehill.rundeckbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigService {
    /**
     * Top level job settings file wrapper.
     */
    data class JobConfigWrapper(val version: String?,
                                val jobs: Map<String, JobConfig>?)

    /**
     * Models a job configuration.
     */
    data class JobConfig(var name: String?,
                         val jobId: String?,
                         val options: Map<String, String>?,
                         val template: String?)

    private val configFileVersion = "1"
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val settings = Settings()

    val jobConfigFileName = "jobs.json"

    fun loadJobs(): Map<String, JobConfig> {
        val jobConfigFile = File(settings.configDir, jobConfigFileName)

        val jobConfig = objectMapper.readValue(jobConfigFile, JobConfigWrapper::class.java) ?:
                throw RuntimeException("Job configuration at ${jobConfigFile} was null")

        assert(configFileVersion == jobConfig.version) {
            "Unsupported job settings version: ${jobConfig.version} (expected '${configFileVersion}')"
        }

        val jobs = jobConfig.jobs ?: throw IllegalStateException("No jobs section found in configuration")

        jobs.forEach { job -> job.value.name = job.key }

        return jobs
    }
}
