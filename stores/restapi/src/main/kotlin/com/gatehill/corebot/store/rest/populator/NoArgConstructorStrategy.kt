package com.gatehill.corebot.store.rest.populator

import com.gatehill.corebot.store.rest.config.StoreSettings
import kotlin.reflect.KClass

/**
 * Assumes a no-arg constructor.
 */
class NoArgConstructorStrategy<out T : Any>(private val clazz: KClass<T>) : PopulationStrategy<T> {
    override fun populate(inputs: Map<String, *>): T {
        val value = clazz.java.newInstance()

        StoreSettings.valueMap.forEach { (source, target) ->
            val sourceField = PopulationStrategy.getAccessibleField(clazz, source)
            sourceField.set(value, inputs[target])
        }

        return value
    }
}
