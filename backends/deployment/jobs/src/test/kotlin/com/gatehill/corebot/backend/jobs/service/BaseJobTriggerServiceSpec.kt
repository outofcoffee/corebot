package com.gatehill.corebot.backend.jobs.service

import com.gatehill.corebot.action.ActionOutcomeService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.test.TestMother
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.amshove.kluent.Verify
import org.amshove.kluent.VerifyNoFurtherInteractions
import org.amshove.kluent.VerifyNotCalled
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be null`
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
 * Specification for `BaseJobTriggerService`.
 */
object BaseJobTriggerServiceSpec : Spek({
    val actionConfig = TestMother.actions.values.first()

    given("a job trigger service") {
        on("triggering an unlocked action") {
            val lockService = mock<LockService> {
                on { findActionLock(any(), any()) } doAnswer {
                    val callback = it.getArgument<(LockService.ActionLock?) -> Unit>(1)
                    callback(null)
                }

                on { findOptionLock(any()) } doReturn null as LockService.OptionLock?
            }

            val service = configureService(lockService)
            val future = CompletableFuture<PerformActionResult>()
            val args = mapOf("unlockedoption" to "unlockedvalue")

            service.trigger(TestMother.trigger, future, actionConfig, args)

            it("is triggered") {
                val actionResult = future.get()
                actionResult.`should not be null`()
                actionResult.finalResult.`should be true`()

                Verify on lockService that lockService.findActionLock(any(), any()) was called
                Verify on lockService that lockService.findOptionLock(any()) was called
                VerifyNoFurtherInteractions on lockService
            }
        }

        on("triggering an action-locked action") {
            val lockService = mock<LockService> {
                on { findActionLock(any(), any()) } doAnswer {
                    val callback = it.getArgument<(LockService.ActionLock?) -> Unit>(1)
                    callback(LockService.ActionLock("owner"))
                }

                on { findOptionLock(any()) } doReturn null as LockService.OptionLock?
            }

            val service = configureService(lockService)
            val future = CompletableFuture<PerformActionResult>()

            service.trigger(TestMother.trigger, future, actionConfig, emptyMap())

            it("fails to trigger") {
                var gotResult = false
                try {
                    future.get()
                    gotResult = true

                } catch (e: Exception) {
                    e.message?.`should contain`("locked by")
                }
                if (gotResult) { throw RuntimeException("Future should complete exceptionally") }

                Verify on lockService that lockService.findActionLock(any(), any()) was called
                VerifyNotCalled on lockService that lockService.findOptionLock(any())
                VerifyNoFurtherInteractions on lockService
            }
        }

        on("triggering an option-locked action") {
            val lockService = mock<LockService> {
                on { findActionLock(any(), any()) } doAnswer {
                    val callback = it.getArgument<(LockService.ActionLock?) -> Unit>(1)
                    callback(null)
                }

                on { findOptionLock(any()) } doReturn
                        LockService.OptionLock("owner", "locked", "lockedvalue")
            }

            val service = configureService(lockService)
            val future = CompletableFuture<PerformActionResult>()
            val args = mapOf("lockedoption" to "lockedvalue")

            service.trigger(TestMother.trigger, future, actionConfig, args)

            it("fails to trigger") {
                var gotResult = false
                try {
                    future.get()
                    gotResult = true

                } catch (e: Exception) {
                    e.message?.`should contain`("locked by")
                }
                if (gotResult) { throw RuntimeException("Future should complete exceptionally") }

                Verify on lockService that lockService.findActionLock(any(), any()) was called
                Verify on lockService that lockService.findOptionLock(any()) was called
                VerifyNoFurtherInteractions on lockService
            }
        }
    }
})

fun configureService(lockService: LockService): BaseJobTriggerService {
    val actionOutcomeService = mock<ActionOutcomeService>()

    return object : BaseJobTriggerService(lockService, actionOutcomeService) {
        override fun triggerExecution(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>) {
            future.complete(PerformActionResult())
        }

        override fun fetchExecutionInfo(trigger: TriggerContext, action: ActionConfig, executionId: Int, startTime: Long) {
        }

        override fun fetchExecutionOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int) {
        }
    }
}
