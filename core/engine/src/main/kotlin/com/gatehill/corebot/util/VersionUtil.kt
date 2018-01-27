package com.gatehill.corebot.util

import java.util.jar.Manifest

/**
 * Provides version information.
 */
object VersionUtil {
    val version: String by lazy {
        VersionUtil::class.java.classLoader.getResources("META-INF/MANIFEST.MF").toList().forEach { manifestUrl ->
            manifestUrl.openStream()?.let {
                Manifest().apply { this.read(it) }.mainAttributes.getValue("Corebot-Version")?.let { manifestVersion ->
                    return@lazy manifestVersion
                }
            }
        }
        return@lazy "unspecified"
    }
}
