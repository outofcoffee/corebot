package com.gatehill.corebot.util

import java.util.jar.Manifest

/**
 * Provides version information.
 */
object VersionUtil {
    const val UNSPECIFIED_VERSION = "unspecified"

    val version: String by lazy {
        VersionUtil::class.java.classLoader.getResources("META-INF/MANIFEST.MF").toList().forEach { manifestUrl ->
            manifestUrl.openStream()?.use { manifestStream ->
                val manifest = Manifest().apply { read(manifestStream) }
                manifest.mainAttributes.getValue("Corebot-Version")?.let { manifestVersion ->
                    return@lazy manifestVersion
                }
            }
        }
        return@lazy UNSPECIFIED_VERSION
    }
}
