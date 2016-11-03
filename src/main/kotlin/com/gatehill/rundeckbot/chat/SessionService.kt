package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.Settings
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object SessionService {
    val session: SlackSession by lazy {
        val session = SlackSessionFactory.createWebSocketSlackSession(Settings.chat.authToken)
        session.connect()
        session
    }

    val botUsername: String
        get() = session.sessionPersona().userName
}
