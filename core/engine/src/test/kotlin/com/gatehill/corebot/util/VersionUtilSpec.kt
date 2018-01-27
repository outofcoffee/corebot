import com.gatehill.corebot.util.VersionUtil
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.`should not equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `VersionUtil`.
 */
object VersionUtilSpec : Spek({
    given("a version utility") {
        on("reading version") {
            val version = VersionUtil.version

            it("should provide a version") {
                version.`should not be null`()
            }
            it("should provide a valid version") {
                version `should not equal` "unspecified"
            }
        }
    }
})
