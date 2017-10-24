package com.gatehill.corebot.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UsersListResponse(val ok: Boolean,
                             val members: List<SlackUser>)
