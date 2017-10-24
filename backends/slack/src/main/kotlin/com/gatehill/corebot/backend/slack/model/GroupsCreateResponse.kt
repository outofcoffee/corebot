package com.gatehill.corebot.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsCreateResponse(val ok: Boolean,
                                val group: SlackGroup)
