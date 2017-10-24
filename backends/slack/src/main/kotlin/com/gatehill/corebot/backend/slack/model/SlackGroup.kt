package com.gatehill.corebot.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackGroup(val id: String,
                      val name: String,
                      val members: List<String>)
