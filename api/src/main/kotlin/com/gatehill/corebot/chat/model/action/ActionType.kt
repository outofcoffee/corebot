package com.gatehill.corebot.chat.model.action

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
enum class ActionType(val description: String) {
    HELP("show help"),
    TRIGGER("trigger job"),
    ENABLE("enable job"),
    DISABLE("disable job"),
    LOCK_ACTION("lock action"),
    UNLOCK_ACTION("unlock action"),
    STATUS("check status"),
    LOCK_OPTION("lock option"),
    UNLOCK_OPTION("unlock option");
}
