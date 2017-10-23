package com.gatehill.corebot.frontend.http.chat

import com.gatehill.corebot.frontend.session.chat.SessionHolder
import com.gatehill.corebot.frontend.session.chat.StatefulSessionService
import io.vertx.ext.web.RoutingContext
import java.util.UUID

interface HttpSessionService : StatefulSessionService<RoutingContext, HttpSessionHolder>

class HttpSessionHolder(session: RoutingContext,
                        username: String = UUID.randomUUID().toString(),
                        realName: String = UUID.randomUUID().toString()) : SessionHolder<RoutingContext>(session, UUID.randomUUID().toString(), username, realName)
