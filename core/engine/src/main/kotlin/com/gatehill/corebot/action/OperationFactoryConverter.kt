package com.gatehill.corebot.action

import com.gatehill.corebot.operation.factory.OperationFactory
import com.gatehill.corebot.config.model.ActionConfig

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface OperationFactoryConverter {
    fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<OperationFactory>
}

/**
 * An implementation that returns an empty `List`.
 */
class NoOpOperationFactoryConverter : OperationFactoryConverter {
    override fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<OperationFactory> = emptyList()
}
