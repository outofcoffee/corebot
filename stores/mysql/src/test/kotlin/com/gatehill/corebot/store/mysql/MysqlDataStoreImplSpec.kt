package com.gatehill.corebot.store.mysql

import com.gatehill.corebot.store.mysql.config.StoreSettings
import com.gatehill.corebot.store.partition
import com.gatehill.corebot.test.KMySQLContainer
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `MysqlDataStoreImpl`.
 */
object MysqlDataStoreImplSpec : Spek({
    given("a MySQL store") {
        val mysql = KMySQLContainer()

        beforeGroup {
            mysql.start()

            StoreSettings.env = mapOf(
                    "MYSQL_CONNECTION_STRING" to "${mysql.jdbcUrl}?useSSL=false",
                    "MYSQL_USERNAME" to mysql.username,
                    "MYSQL_PASSWORD" to mysql.password
            )
        }

        on("getting a partition") {
            val store = MysqlDataStoreImpl()
            val partition = store.partition<String, Example>("test")

            it("saves objects") {
                partition["key"] = Example("bar")
            }

            it("loads saved objects") {
                val savedValue = partition["key"]
                savedValue.`should not be null`()
                savedValue!!.foo `should equal` "bar"
            }

            it("updates existing objects") {
                partition["key"] = Example("baz")

                val updatedValue = partition["key"]
                updatedValue.`should not be null`()
                updatedValue!!.foo `should equal` "baz"
            }

            it("removes saved objects") {
                partition.remove("key")
                partition["key"].`should be null`()
            }
        }

        afterGroup {
            mysql.stop()
        }
    }
})

data class Example(val foo: String)
