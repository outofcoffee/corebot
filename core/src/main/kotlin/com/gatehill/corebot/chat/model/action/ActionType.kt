package com.gatehill.corebot.chat.model.action

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
enum class ActionType(val description: String) {
    TRIGGER("trigger job"),
    ENABLE("enable job"),
    DISABLE("disable job"),
    LOCK("lock"),
    UNLOCK("unlock"),
    STATUS("check status"),
    HELP("show help")
}
