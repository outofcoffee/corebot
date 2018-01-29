package com.gatehill.corebot.store.rest

import com.gatehill.corebot.store.partition
import com.gatehill.corebot.store.rest.config.StoreSettings
import com.gatehill.corebot.store.rest.util.CrudServer
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.fail

/**
 * Specification for `RestDataStoreImpl`.
 */
object RestDataStoreImplSpec : Spek({
    val crudServer = CrudServer()

    beforeGroup {
        crudServer.start()
    }

    given("a RESTful store") {
        on("getting a simple partition") {
            StoreSettings.env = mapOf(
                    "REST_BASE_URL" to "http://localhost:${crudServer.port}/",
                    "REST_RESOURCE" to "crud",
                    "REST_KEY_FIELD" to "name",
                    "REST_VALUE_MAP" to "original=modified"
            )

            val store = RestDataStoreImpl()
            val partition = store.partition<String, Example>("test")

            it("saves simple objects") {
                partition["test"] = Example("qux")
            }
            it("loads simple objects") {
                partition["test"]!!.original `should equal` "qux"
            }
            it("returns null for missing objects") {
                partition["missing"].`should be null`()
            }
        }

        on("getting a complex partition") {
            StoreSettings.env = mapOf(
                    "REST_BASE_URL" to "http://localhost:${crudServer.port}/",
                    "REST_RESOURCE" to "crud",
                    "REST_KEY_FIELD" to "name",
                    "REST_JSON_PATH" to "$.examples[0]",
                    "REST_VALUE_MAP" to "original=modified"
            )

            val store = RestDataStoreImpl()
            val partition = store.partition<String, ExampleHolder>("test")

            it("saves complex objects") {
                partition["test"] = ExampleHolder(examples = listOf(Example("quux")))
            }
            it("loads complex objects") {
                try {
                    partition["test"]!!.examples
                    fail("Should have thrown IllegalArgumentException")
                } catch (e: IllegalArgumentException) {
                    e.message!! `should contain` "JsonPath"
                }
            }
        }
    }

    afterGroup {
        crudServer.stop()
    }
})

data class Example(val original: String)

data class ExampleHolder(val examples: List<Example>)
