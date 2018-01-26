package com.gatehill.corebot.store.rest

import com.gatehill.corebot.store.partition
import com.gatehill.corebot.store.rest.config.StoreSettings
import com.gatehill.corebot.store.rest.util.CrudServer
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `RestDataStoreImpl`.
 */
object RestDataStoreImplSpec : Spek({
    val crudServer = CrudServer()

    beforeGroup {
        crudServer.start()
    }

    given("a RESTful store") {
        beforeGroup {
            StoreSettings.env = mapOf(
                    "REST_BASE_URL" to "http://localhost:${crudServer.port}/",
                    "REST_RESOURCE" to "crud",
                    "REST_KEY_FIELD" to "name",
                    "REST_VALUE_MAP" to "original=modified"
            )
        }

        on("getting a partition") {
            val store = RestDataStoreImpl()
            val partition = store.partition<String, Example>("test")

            it("saves objects") {
                partition["test"] = Example("qux")
            }
            it("loads objects") {
                partition["test"]!!.original `should equal` "qux"
            }
            it("returns null for missing objects") {
                partition["missing"].`should be null`()
            }
        }
    }

    afterGroup {
        crudServer.stop()
    }
})

data class Example(val original: String)
