package com.gatehill.corebot.plugin.config

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object PluginSettings {
    val pluginsFile: Path by lazy {
        System.getenv("PLUGIN_CONFIG_FILE")?.let { Paths.get(it) }
                ?: throw IllegalStateException("Missing plugin configuration file")
    }

    val localRepo: String by lazy { System.getenv("PLUGIN_LOCAL_REPO") ?: "/opt/corebot/plugins" }
}
