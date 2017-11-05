package com.gatehill.corebot.backend.rundeck.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Models a job.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RundeckJob(
        /**
         * UUID
         */
        val id: String,

        /**
         * Job name
         */
        val name: String,

        /**
         * Group
         */
        val group: String,

        /**
         * Project
         */
        val project: String,

        /**
         * Job description
         */
        val description: String,

        /**
         * API url
         */
        val href: String,

        /**
         * GUI url
         */
        val permalink: String,

        /**
         * Whether the job is scheduled.
         */
        val scheduled: Boolean,

        /**
         * Whether scheduling is enabled.
         */
        val scheduleEnabled: Boolean,

        /**
         * Whether the job is enabled.
         */
        val enabled: Boolean
)
