package com.gatehill.corebot.frontend.websocket.chat.endpoint

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class CustomConfigurator : javax.websocket.server.ServerEndpointConfig.Configurator() {
    override fun <T> getEndpointInstance(endpointClass: Class<T>): T {
        return injector.getInstance(endpointClass)
    }

    companion object {
        @javax.inject.Inject
        private lateinit var injector: com.google.inject.Injector
    }
}
