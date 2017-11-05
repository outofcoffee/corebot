package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.action.ActionOutcomeService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.backend.rundeck.config.DriverSettings
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.SystemConfig
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.test.KRundeckContainer
import com.gatehill.corebot.test.TestMother
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.any
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

/**
 * Specification for `RundeckJobTriggerService`.
 */
object RundeckJobTriggerServiceSpec : Spek({
    val projectName = "test"

    given("a rundeck job trigger service") {
        val rundeck = KRundeckContainer()

        beforeGroup {
            rundeck.start()

            // authenticate, create a project and upload a job
            val sessionId = TestUtil.fetchSessionId(rundeck).get()
            TestUtil.createProject(rundeck, sessionId, projectName)
            TestUtil.importJob(rundeck, sessionId, projectName,
                    Paths.get(RundeckJobTriggerServiceSpec::class.java.getResource("/test-job.yml").toURI()))

            // the token to use for API calls
            val authToken = TestUtil.fetchTokenFromSession(rundeck, sessionId)

            DriverSettings.env = mapOf(
                    "RUNDECK_BASE_URL" to rundeck.baseUrl,
                    "RUNDECK_API_TOKEN" to authToken
            )
        }

        val lockService = mock<LockService> {
            on { findActionLock(any(), any()) } doAnswer {
                val callback = it.getArgument<(LockService.ActionLock?) -> Unit>(1)
                callback(null)
            }
            on { findOptionLock(any(), any(), any()) } doReturn null as LockService.OptionLock?
        }

        val configService = mock<ConfigService> {
            on { system() } doReturn SystemConfig()
        }

        // a real API client builder
        val apiClientBuilder = RundeckApiClientBuilder(configService)

        val actionOutcomeService = mock<ActionOutcomeService>()

        val service = RundeckJobTriggerService(
                apiClientBuilder,
                lockService,
                actionOutcomeService
        )

        on("triggering a job") {
            val future = CompletableFuture<PerformActionResult>()
            val action = TestMother.actions.values.first()
            val args = mapOf<String, String>()
            service.trigger(TestMother.trigger, future, action, args)

            it("is triggered successfully") {
                val actionResult = future.get()
                actionResult.message?.`should not be null`()
                actionResult.message!!.toLowerCase() `should contain` "running"
            }
        }

        afterGroup {
            rundeck.stop()
        }
    }
})
