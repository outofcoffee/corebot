import com.gatehill.corebot.operation.factory.OperationFactory
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.factory.readOperationFactoryMetadata
import com.gatehill.corebot.chat.filter.RegexFilter
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
import java.util.regex.Pattern

/**
 * Specification for `RegexFilter`.
 */
object RegexFilterSpec : Spek({
    given("a regex filter") {
        val filter = RegexFilter()

        val mockOperationFactory = { values: MutableMap<String, String> ->
            @Template("dummy",
                    builtIn = true,
                    showInUsage = true,
                    operationMessageMode = OperationMessageMode.INDIVIDUAL,
                    placeholderKeys = arrayOf(placeholderKey))
            abstract class DummyFactory : OperationFactory

            mock<OperationFactory> {
                on { placeholderValues } doReturn values
                on { readMetadata() } doReturn readOperationFactoryMetadata(DummyFactory::class.java)
                on { onSatisfied() } doReturn true
            }
        }

        on("parsing a valid placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockOperationFactory(placeholderValues)
            val config = RegexFilter.RegexFilterConfig(Pattern.compile(templateRegex), null)
            val match = filter.matches(config, factory, "test $placeholderValue")

            it("should match") {
                match.`should be true`()
            }

            it("should have set the placeholder value") {
                verify(factory).placeholderValues
                placeholderValues[placeholderKey] `should equal` placeholderValue
            }
        }

        on("parsing an invalid placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockOperationFactory(placeholderValues)
            val config = RegexFilter.RegexFilterConfig(Pattern.compile(templateRegex), null)
            val match = filter.matches(config, factory, "non matching")

            it("should not match") {
                match.`should be false`()
            }

            it("should not have set the placeholder value") {
                verify(factory, never()).placeholderValues
                placeholderValues[placeholderKey].`should be null`()
            }
        }
    }
})
