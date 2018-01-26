package com.gatehill.corebot.store.rest.populator

import java.lang.reflect.Field
import kotlin.reflect.KClass

interface PopulationStrategy<out T> {
    fun populate(inputs: Map<String, *>): T

    companion object {
        fun <T : Any> infer(clazz: KClass<T>) = if (clazz.constructors.first().parameters.isEmpty()) {
            NoArgConstructorStrategy(clazz)
        } else {
            FinalFieldConstructorStrategy(clazz)
        }

        fun <T : Any> getAccessibleField(clazz: KClass<T>, fieldName: String): Field = clazz.java.getDeclaredField(fieldName).apply {
            isAccessible = true
        }
    }
}
