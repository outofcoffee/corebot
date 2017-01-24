package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionRequest
import com.gatehill.corebot.action.model.PerformActionResult
import java.util.concurrent.CompletableFuture

/**
 * Responsible for performing actions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionPerformService {
    fun perform(request: PerformActionRequest): CompletableFuture<PerformActionResult>
}
