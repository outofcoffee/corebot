package com.gatehill.corebot.action.driver

import com.google.inject.Injector
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

/**
 * Obtain an action driver by alias or fully qualified class name.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ActionDriverFactory @Inject constructor(private val injector: Injector) {
    private val logger = LogManager.getLogger(ActionDriverFactory::class.java)!!
    private val drivers = mutableMapOf<String, Class<out ActionDriver>>()

    fun driverFor(actionDriverName: String): ActionDriver {
        try {
            @Suppress("UNCHECKED_CAST")
            val driverClass: Class<out ActionDriver> = drivers[actionDriverName] ?:
                    Class.forName(actionDriverName) as Class<out ActionDriver>

            logger.trace("Loading driver $driverClass for action driver: $actionDriverName")
            return injector.getInstance(driverClass)

        } catch(e: Exception) {
            throw UnsupportedOperationException("Action driver '$actionDriverName' not supported")
        }
    }

    fun registerDriver(name: String, driver: Class<out ActionDriver>) {
        logger.debug("Registering '$name' driver: ${driver.canonicalName}")
        drivers[name] = driver
    }
}
