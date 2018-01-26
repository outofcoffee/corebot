package com.gatehill.corebot.store.rest.util

import com.gatehill.corebot.util.jsonMapper
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.ServerSocket

/**
 * RESTful CRUD server for testing.
 */
class CrudServer {
    val port = ServerSocket(0).use { it.localPort }

    private val httpServer = HttpServer.create(InetSocketAddress("localhost", port), port)

    private val store = mutableMapOf<String, MutableMap<String, String>>()

    fun start() {
        buildCrudStore(httpServer)
        httpServer.start()
    }

    fun stop() {
        httpServer.stop(0)
    }

    private fun buildCrudStore(httpServer: HttpServer) {
        httpServer.createContext("/crud") { httpExchange ->
            when (httpExchange.requestMethod) {
                "GET" -> {
                    val name = httpExchange.requestURI.query.split("&")
                            .map { it.split("=").let { it[0] to it[1] } }
                            .first { it.first == "name" }
                            .second

                    val itemStore = store[name] ?: emptyMap<String, String>()
                    val body = jsonMapper.writeValueAsBytes(itemStore)

                    httpExchange.sendResponseHeaders(200, body.size.toLong())
                    httpExchange.responseBody.write(body)
                    httpExchange.close()
                }
                "POST" -> {
                    val requestMap = jsonMapper.readValue(httpExchange.requestBody, Map::class.java)
                    val name = requestMap["name"] as String

                    val itemStore = store[name] ?: mutableMapOf()
                    requestMap.forEach { (key, value) -> itemStore[key as String] = value as String }
                    store[name] = itemStore

                    httpExchange.sendResponseHeaders(200, 0)
                    httpExchange.close()
                }
            }
        }
    }
}
