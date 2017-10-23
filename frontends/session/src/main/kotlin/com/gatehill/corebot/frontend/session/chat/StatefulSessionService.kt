package com.gatehill.corebot.frontend.session.chat

import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.operation.model.TriggerContext

interface StatefulSessionService : SessionService {
    val connectedSessions: MutableList<SessionHolder<*>>

    fun findTriggerSession(trigger: TriggerContext): SessionHolder<*>
    fun findTriggerSession(sessionId: String): SessionHolder<*>
    fun terminateSession(trigger: TriggerContext)
}

abstract class SessionHolder<out S>(val session: S,
                                    var sessionId: String,
                                    var username: String,
                                    var realName: String) {

    override fun toString(): String {
        return "SessionHolder(session=$session, sessionId='$sessionId', username='$username', realName='$realName')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionHolder<*>) return false

        if (sessionId != other.sessionId) return false

        return true
    }

    override fun hashCode(): Int {
        return sessionId.hashCode()
    }
}
