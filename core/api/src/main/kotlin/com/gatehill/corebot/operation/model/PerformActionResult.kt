package com.gatehill.corebot.operation.model

/**
 * The result of performing an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PerformActionResult(val message: String? = null,
                               val finalResult: Boolean = true)
