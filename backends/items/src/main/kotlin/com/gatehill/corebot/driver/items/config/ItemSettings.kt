package com.gatehill.corebot.driver.items.config

/**
 * Item driver settings.
 */
object ItemSettings {
    val showStatusOnChange by lazy { System.getenv("ITEMS_SHOW_STATUS_ON_CHANGE")?.toBoolean() ?: false }
}
