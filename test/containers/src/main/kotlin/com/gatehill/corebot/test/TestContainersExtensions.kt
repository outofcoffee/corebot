package com.gatehill.corebot.test

import org.apache.logging.log4j.LogManager
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.time.Duration

class KMySQLContainer : MySQLContainer<KMySQLContainer>("${MySQLContainer.IMAGE}:5.7") {
    val logger = LogManager.getLogger("mysql")

    override fun start() {
        super.start()
        followOutput { logger.debug(it) }
    }
    override fun getJdbcUrl(): String = "${super.getJdbcUrl()}?useSSL=false"
}

class KRedisContainer : GenericContainer<KRedisContainer>("redis:latest") {
    init {
        withExposedPorts(6379)
        waitingFor(HostPortWaitStrategy())
    }

    override fun start() {
        super.start()
        followOutput { logger.debug(it) }
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
