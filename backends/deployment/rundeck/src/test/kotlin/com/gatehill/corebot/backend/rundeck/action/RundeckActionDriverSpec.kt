package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.backend.jobs.operation.factory.JobOperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.test.TestMother
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import org.amshove.kluent.Verify
import org.amshove.kluent.`should be true`
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.CompletableFuture

/**
 * Specification for `RundeckActionDriver`.
 */
object RundeckActionDriverSpec : Spek({
    given("a rundeck action driver") {
        val triggerJobService = mock<RundeckJobTriggerService> {
            on { trigger(any(), any(), any(), any()) } doAnswer {
                val future = it.getArgument<CompletableFuture<PerformActionResult>>(1)
                future.complete(PerformActionResult())
                Unit
            }
        }

        val lockService = mock<LockService>()
        val executionStatusService = mock<ExecutionStatusService>()

        val driver = RundeckActionDriverImpl(
                triggerJobService,
                lockService,
                executionStatusService
        )

        on("triggering a job") {
            val action = TestMother.actions.values.first()
            val args = mapOf<String, String>()
            val future = driver.perform(TestMother.trigger, JobOperationType.TRIGGER, action, args)

            it("is triggered") {
                val actionResult = future.get()
                actionResult.finalResult.`should be true`()

                Verify on triggerJobService that triggerJobService.trigger(any(), any(), any(), any()) was called
            }
        }
    }
})
