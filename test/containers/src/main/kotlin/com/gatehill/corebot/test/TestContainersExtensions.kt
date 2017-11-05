package com.gatehill.corebot.test

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.HostPortWaitStrategy
import java.time.Duration

class KMySQLContainer : MySQLContainer<KMySQLContainer>()

class KRedisContainer : GenericContainer<KRedisContainer>("redis:latest") {
    init {
        withExposedPorts(6379)
        waitingFor(HostPortWaitStrategy())
    }
}

class KRundeckContainer : GenericContainer<KRundeckContainer>("jordan/rundeck:latest") {
    init {
        withExposedPorts(4440)
        waitingFor(HostPortWaitStrategy().withStartupTimeout(Duration.ofSeconds(120)))
        withEnv(mapOf(
                "SERVER_URL" to "http://localhost:4440"
        ))
    }

    val baseUrl: String
        get() = "http://$containerIpAddress:${getMappedPort(4440)}/"
}
