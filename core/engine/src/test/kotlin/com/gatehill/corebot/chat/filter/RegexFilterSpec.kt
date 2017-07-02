import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.factory.readActionFactoryMetadata
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

        val mockActionFactory = { values: MutableMap<String, String> ->
            @Template("dummy",
                    builtIn = true,
                    showInUsage = true,
                    actionMessageMode = ActionMessageMode.INDIVIDUAL,
                    placeholderKeys = arrayOf(placeholderKey))
            abstract class DummyFactory : ActionFactory

            mock<ActionFactory> {
                on { placeholderValues } doReturn values
                on { readMetadata() } doReturn readActionFactoryMetadata(DummyFactory::class.java)
                on { onSatisfied() } doReturn true
            }
        }

        on("parsing a valid placeholder") {
            val placeholderValues = mutableMapOf<String, String>()
            val factory = mockActionFactory(placeholderValues)
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
            val factory = mockActionFactory(placeholderValues)
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
