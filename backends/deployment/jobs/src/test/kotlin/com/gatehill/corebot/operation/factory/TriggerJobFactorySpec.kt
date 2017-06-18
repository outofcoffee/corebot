import com.gatehill.corebot.operation.factory.NamedActionFactory
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.backend.jobs.action.factory.TriggerJobFactory
import com.gatehill.corebot.test.TestMother
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `TriggerJobFactory`.
 */
object TriggerJobFactorySpec : Spek({
    given("a trigger job factory") {
        val chatGenerator = mock<ChatGenerator> {
            on { pleaseWait() } doReturn "please wait"
        }
        val factory = TriggerJobFactory(TestMother.actions.values.first(), chatGenerator)

        on("providing placeholders") {
            factory.placeholderValues += NamedActionFactory.actionPlaceholder to TestMother.actionName
            val satisfied = factory.onSatisfied()

            it("should be satisfied") {
                satisfied `should be` true
            }
        }

        on("building operations") {
            val operations = factory.buildOperations(TestMother.trigger)

            it("should produce operations") {
                operations.size `should equal` 1
            }

            it("should produce operations of the correct type") {
                operations.first().operationType `should equal` factory.operationType
            }

            it("should have called the chat generator") {
                verify(chatGenerator).pleaseWait()
            }
        }
    }
})
