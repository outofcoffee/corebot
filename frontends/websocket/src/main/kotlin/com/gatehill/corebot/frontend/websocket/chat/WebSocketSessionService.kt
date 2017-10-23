package com.gatehill.corebot.frontend.websocket.chat

import com.gatehill.corebot.frontend.session.chat.SessionHolder
import com.gatehill.corebot.frontend.session.chat.StatefulSessionService
import javax.websocket.Session

interface WebSocketSessionService : StatefulSessionService {
    fun broadcastToAll(message: String)
}

class WebSocketSessionHolder(session: Session,
                             username: String = session.id,
                             realName: String = session.id) : SessionHolder<Session>(session, session.id, username, realName)

val SessionHolder<*>.wsSession
    get() = this as WebSocketSessionHolder
