import com.gatehill.corebot.operation.factory.OperationFactory
import com.gatehill.corebot.chat.filter.StringFilter
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `StringFilter`.
 */
object StringFilterSpec : Spek({
    given("a string filter") {
        val filter = StringFilter()

        val mockOperationFactory = { values: MutableMap<String, String> ->
            mock<OperationFactory> {
                on { placeholderValues } doReturn values
                on { onSatisfied() } doReturn true
            }
        }

        on("parsing a valid single word placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockOperationFactory(placeholderValues)
            val config = StringFilter.StringFilterConfig("test {$placeholderKeySingleWord}", null)
            val match = filter.matches(config, factory, "test $placeholderValue")

            it("should match") {
                match.`should be true`()
            }

            it("should have set the placeholder value") {
                verify(factory).placeholderValues
                placeholderValues[placeholderKeySingleWord] `should equal` placeholderValue
            }
        }

        on("parsing a valid multi-word placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockOperationFactory(placeholderValues)
            val config = StringFilter.StringFilterConfig("test {$placeholderKeyMultiword}", null)
            val match = filter.matches(config, factory, "test $placeholderValue")

            it("should match") {
                match.`should be true`()
            }

            it("should have set the placeholder value") {
                verify(factory).placeholderValues
                placeholderValues[placeholderKeyMultiword] `should equal` placeholderValue
            }
        }

        on("parsing an invalid placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockOperationFactory(placeholderValues)
            val config = StringFilter.StringFilterConfig("test {$placeholderKeySingleWord}", null)
            val match = filter.matches(config, factory, "non matching")

            it("should not match") {
                match.`should be false`()
            }

            it("should not have set the placeholder value") {
                verify(factory, never()).placeholderValues
                placeholderValues[placeholderKeySingleWord].`should be null`()
            }
        }
    }
})
