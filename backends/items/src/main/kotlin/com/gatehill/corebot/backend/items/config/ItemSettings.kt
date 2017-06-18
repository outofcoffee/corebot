package com.gatehill.corebot.backend.items.config

/**
 * Item driver settings.
 */
object ItemSettings {
    /**
     * Whether to show the status on every change.
     */
    val showStatusOnChange by lazy { System.getenv("ITEMS_SHOW_STATUS_ON_CHANGE")?.toBoolean() ?: false }

    /**
     * How to display the owner.
     */
    val ownerDisplayMode: OwnerDisplayMode by lazy {
        System.getenv("ITEMS_OWNER_DISPLAY")?.let { OwnerDisplayMode.valueOf(it) } ?: OwnerDisplayMode.USERNAME
    }
}

/**
 * How to display the owner.
 */
enum class OwnerDisplayMode {
    USERNAME,
    REAL_NAME
}
