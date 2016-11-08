package com.gatehill.corebot.action

import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
import com.google.inject.Injector
import javax.inject.Inject

/**
 * Obtain an action driver by alias or fully qualified class name.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ActionDriverFactory @Inject constructor(private val injector: Injector) {
    fun driverFor(actionDriverName: String): ActionDriver {
        try {
            @Suppress("UNCHECKED_CAST")
            val driverClass: Class<out ActionDriver> = when (actionDriverName) {
                "rundeck" -> RundeckActionDriver::class.java
                else -> Class.forName(actionDriverName) as Class<out ActionDriver>
            }

            return injector.getInstance(driverClass)

        } catch(e: Exception) {
            throw UnsupportedOperationException("Action driver '${actionDriverName}' not supported")
        }
    }
}
