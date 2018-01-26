package com.gatehill.corebot.util

import java.util.jar.Manifest

/**
 * Provides version information.
 */
object VersionUtil {
    val version: String by lazy {
        Manifest().mainAttributes["Corebot-Version"] as String? ?: "unspecified"
    }
}
