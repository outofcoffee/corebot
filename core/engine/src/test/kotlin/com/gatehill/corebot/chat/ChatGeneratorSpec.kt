import com.gatehill.corebot.chat.ChatGenerator
import org.amshove.kluent.`should end with`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should start with`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `ChatGenerator`.
 */
object ChatGeneratorSpec : Spek({
    given("a chat generator") {
        val chatGenerator = ChatGenerator(mapOf(
                "goodNewsEmoji" to listOf("emoji"),
                "goodNews" to listOf("good news \${goodNewsEmoji}")
        ))

        on("generating a simple line") {
            val line = chatGenerator.goodNewsEmoji()

            it("should produce the right phrase") {
                line `should equal` "emoji"
            }
        }

        on("generating a complex line") {
            val line = chatGenerator.goodNews()

            it("should start with the right phrase") {
                line `should start with` "good news"
            }
            it("should have resolved the placeholder") {
                line `should end with` "emoji"
            }
        }
    }
})
