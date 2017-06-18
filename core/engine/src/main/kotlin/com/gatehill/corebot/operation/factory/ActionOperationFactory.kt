package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.Operation
import com.gatehill.corebot.operation.model.ActionOperation
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.TransformType
import java.util.HashMap

/**
 * Represents an action operation.
 */
abstract class ActionOperationFactory : BaseOperationFactory() {
    abstract val actionConfigs : List<ActionConfig>

    /**
     * List the operations from this template.
     */
    override fun buildOperations(trigger: TriggerContext): List<Operation> {
        return actionConfigs.map { actionConfig ->
            val options = transform(actionConfig, placeholderValues)
            ActionOperation(operationType,
                    this,
                    buildShortDescription(actionConfig),
                    if (operationMessageMode == OperationMessageMode.INDIVIDUAL && actionConfig.showJobOutcome) buildStartMessage(trigger, options, actionConfig) else null,
                    actionConfig.tags,
                    actionConfig.driver,
                    actionConfig,
                    options)
        }
    }

    private fun transform(actionConfig: ActionConfig, options: MutableMap<String, String>): Map<String, String> {
        val transformed: MutableMap<String, String> = HashMap(options)

        actionConfig.options.map { Pair(it.key, it.value.transformers) }.forEach { optionTransform ->
            val optionKey = optionTransform.first

            var optionValue = options[optionKey]
            optionValue?.let {
                optionTransform.second.forEach { transformType ->
                    optionValue = when (transformType) {
                        TransformType.LOWERCASE -> optionValue!!.toLowerCase()
                        TransformType.UPPERCASE -> optionValue!!.toUpperCase()
                        else -> throw UnsupportedOperationException("Transform type $transformType is not supported")
                    }
                }
                transformed[optionKey] = optionValue!!
            }
        }

        return transformed
    }
}
