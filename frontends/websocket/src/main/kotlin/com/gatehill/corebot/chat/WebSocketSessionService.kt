package com.gatehill.corebot.chat

import com.gatehill.corebot.action.model.TriggerContext
import javax.websocket.Session

interface WebSocketSessionService : SessionService {
    val connectedSessions: MutableList<SessionHolder>

    fun broadcastToAll(message: String)
    fun findTriggerSession(trigger: TriggerContext): SessionHolder
    fun findTriggerSession(sessionId: String): SessionHolder
}

data class SessionHolder(val session: Session,
                         var username: String = session.id,
                         var realName: String = session.id)
