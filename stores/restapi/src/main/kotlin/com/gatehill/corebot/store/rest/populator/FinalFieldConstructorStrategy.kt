package com.gatehill.corebot.store.rest.populator

import com.gatehill.corebot.store.rest.config.StoreSettings
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Assumes final fields with values passed in constructor, such as
 * a data class.
 */
class FinalFieldConstructorStrategy<out T : Any>(private val clazz: KClass<T>) : PopulationStrategy<T> {
    override fun populate(inputs: Map<String, *>): T {
        // first constructor with args
        val ctor = clazz.primaryConstructor!!

        val paramValues = ctor.parameters.map { parameter ->
            val sourceName = StoreSettings.valueMap[parameter.name]
            parameter to inputs[sourceName]
        }.toMap()

        return ctor.callBy(paramValues)
    }
}
