package com.gatehill.corebot.plugin.model

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PluginWrapper(val frontends: List<PluginEntry> = emptyList(),
                         val backends: List<PluginEntry> = emptyList(),
                         val storage: List<StorageEntry> = emptyList())
