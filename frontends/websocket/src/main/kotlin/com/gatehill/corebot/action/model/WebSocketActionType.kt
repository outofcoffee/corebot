package com.gatehill.corebot.action.model

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class WebSocketActionType(name: String, description: String) : ActionType(name, description) {
    companion object {
        val SET_REAL_NAME = ActionType("SET_REAL_NAME", "set real name")
        val SET_USERNAME = ActionType("SET_USERNAME", "set username")
    }
}
