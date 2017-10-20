package com.gatehill.corebot.classloader

/**
 * Holds the `ClassLoader` used to load injection modules and resources.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ClassLoaderUtil {
    var classLoader: ClassLoader = ClassLoaderUtil::class.java.classLoader
}
