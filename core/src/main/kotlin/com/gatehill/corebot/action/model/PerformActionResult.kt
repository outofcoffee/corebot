package com.gatehill.corebot.action.model

/**
 * The result of performing an action.
 */
data class PerformActionResult(val message: String? = null,
                               val finalResult: Boolean = true)
