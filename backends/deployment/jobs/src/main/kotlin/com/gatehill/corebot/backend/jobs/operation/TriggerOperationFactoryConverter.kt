package com.gatehill.corebot.backend.jobs.operation

import com.gatehill.corebot.operation.OperationFactoryConverter
import com.gatehill.corebot.backend.jobs.operation.factory.TriggerJobFactory
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerOperationFactoryConverter @Inject constructor(private val chatGenerator: ChatGenerator) : OperationFactoryConverter {
    companion object {
        private val jobTriggerDrivers = arrayOf(
                "jenkins",
                "rundeck"
        )
    }

    override fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<OperationFactory> =
            configs.filter { jobTriggerDrivers.contains(it.driver) }
                    .map { TriggerJobFactory(it, chatGenerator) }.toList()
}
