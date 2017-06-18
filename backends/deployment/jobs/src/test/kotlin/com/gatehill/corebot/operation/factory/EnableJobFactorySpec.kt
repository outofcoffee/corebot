import com.gatehill.corebot.operation.factory.NamedActionFactory
import com.gatehill.corebot.test.TestMother
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.backend.jobs.action.factory.EnableJobFactory
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
 * Specification for `EnableJobFactory`.
 */
object EnableJobFactorySpec : Spek({
    given("an enable job factory") {
        val configService = mock<ConfigService> {
            on { actions() } doReturn TestMother.actions
        }
        val factory = EnableJobFactory(configService)

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

            it("should have loaded operations") {
                verify(configService).actions()
            }
        }
    }
})
