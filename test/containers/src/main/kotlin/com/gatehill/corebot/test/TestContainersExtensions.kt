package com.gatehill.corebot.test

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.HostPortWaitStrategy

class KMySQLContainer : MySQLContainer<KMySQLContainer>()

class KRedisContainer : GenericContainer<KRedisContainer>("redis:latest") {
    init {
        withExposedPorts(6379)
        waitingFor(HostPortWaitStrategy())
    }
}
