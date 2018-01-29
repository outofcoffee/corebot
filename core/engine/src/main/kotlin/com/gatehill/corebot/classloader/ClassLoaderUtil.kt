package com.gatehill.corebot.classloader

/**
 * Holds the `ClassLoader` used to load injection modules and resources.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ClassLoaderUtil {
    var classLoader: ClassLoader = ClassLoaderUtil::class.java.classLoader

    @Suppress("UNCHECKED_CAST")
    fun <T> loadClass(clazz: String) = classLoader.loadClass(clazz) as Class<T>
}
