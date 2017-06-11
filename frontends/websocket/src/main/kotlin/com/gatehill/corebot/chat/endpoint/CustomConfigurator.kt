package com.gatehill.corebot.chat.endpoint

class CustomConfigurator : javax.websocket.server.ServerEndpointConfig.Configurator() {
    override fun <T> getEndpointInstance(endpointClass: Class<T>): T {
        return com.gatehill.corebot.chat.endpoint.CustomConfigurator.Companion.injector.getInstance(endpointClass)
    }

    companion object {
        @javax.inject.Inject
        private lateinit var injector: com.google.inject.Injector
    }
}
