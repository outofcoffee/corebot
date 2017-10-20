import com.gatehill.corebot.chat.filter.RegexFilter
import com.gatehill.corebot.chat.filter.StringFilter
import com.gatehill.corebot.chat.template.TemplateService
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `TemplateService`.
 */
object TemplateServiceSpec : Spek({
    given("a template config service") {
        val service = TemplateService()

        on("registering a template file") {
            service.registerClasspathTemplateFile("/test-templates.yml")

            val config = service.loadFilterConfig("test")

            it("should read the template config") {
                config.`should not be null`()
                config.size `should equal` 2
            }

            it("should parse the string template") {
                val stringFilterConfig = config.find { it is StringFilter.StringFilterConfig }
                stringFilterConfig.`should not be null`()
                (stringFilterConfig as StringFilter.StringFilterConfig).template `should equal` "stringTemplate"
            }

            it("should parse the regex template") {
                val regexFilterConfig = config.find { it is RegexFilter.RegexFilterConfig }
                regexFilterConfig.`should not be null`()
                (regexFilterConfig as RegexFilter.RegexFilterConfig).template.pattern() `should equal` "regexTemplate"
            }
        }
    }
})
