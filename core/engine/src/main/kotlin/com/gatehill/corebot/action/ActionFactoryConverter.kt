package com.gatehill.corebot.action

import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.config.model.ActionConfig

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionFactoryConverter {
    fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<ActionFactory>
}

/**
 * An implementation that returns an empty `List`.
 */
class NoOpActionFactoryConverter : ActionFactoryConverter {
    override fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<ActionFactory> = emptyList()
}
