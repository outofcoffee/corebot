package com.gatehill.corebot.frontend.websocket.chat

import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.operation.model.TriggerContext
import javax.websocket.Session

interface WebSocketSessionService : SessionService {
    val connectedSessions: MutableList<SessionHolder>

    fun broadcastToAll(message: String)
    fun findTriggerSession(trigger: TriggerContext): SessionHolder
    fun findTriggerSession(sessionId: String): SessionHolder
    fun terminateSession(trigger: TriggerContext)
}

data class SessionHolder(val session: Session,
                         var username: String = session.id,
                         var realName: String = session.id)
