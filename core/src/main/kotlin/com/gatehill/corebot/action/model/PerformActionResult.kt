package com.gatehill.corebot.action.model

/**
 * The result of performing an action.
 */
data class PerformActionResult(val message: String,
                               val finalResult: Boolean = true)
