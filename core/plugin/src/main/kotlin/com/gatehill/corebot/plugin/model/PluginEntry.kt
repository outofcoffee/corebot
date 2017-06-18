package com.gatehill.corebot.plugin.model

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PluginEntry(val dependency: String,
                       val classes: List<String> = emptyList())
