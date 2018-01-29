package com.gatehill.corebot.store.mirror

import com.gatehill.corebot.store.InMemoryDataStoreImpl
import com.gatehill.corebot.store.mirror.config.StoreSettings
import com.gatehill.corebot.store.mirror.support.TestDataStore
import com.gatehill.corebot.store.partition
import com.google.inject.Guice
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `WriteMirrorDataStoreImpl`.
 */
object WriteMirrorDataStoreImplSpec : Spek({
    val injector = Guice.createInjector()

    given("a write mirror store") {
        on("getting a simple partition") {
            TestDataStore.instances.clear()

            StoreSettings.env = mapOf(
                    "MIRRORED_BACKING_STORE_IMPL" to InMemoryDataStoreImpl::class.java.canonicalName,
                    "MIRROR_DATA_STORE_IMPL" to TestDataStore::class.java.canonicalName
            )

            val store = WriteMirrorDataStoreImpl(injector)
            val partition = store.partition<String, Example>("simple")

            it("saves simple objects to backing store") {
                partition["test"] = Example("qux")
            }
            it("loads simple objects to backing store") {
                partition["test"]?.original `should equal` "qux"
            }
            it("mirrors simple objects to mirror store") {
                val mirrorStore = TestDataStore.instances.first()
                val mirrorPartition = mirrorStore.partition<String, Example>("simple")

                mirrorPartition["test"]?.original `should equal` "qux"
            }
            it("returns null for missing objects") {
                partition["missing"].`should be null`()
            }
        }

        on("getting a complex partition") {
            TestDataStore.instances.clear()

            StoreSettings.env = mapOf(
                    "MIRRORED_BACKING_STORE_IMPL" to InMemoryDataStoreImpl::class.java.canonicalName,
                    "MIRROR_DATA_STORE_IMPL" to TestDataStore::class.java.canonicalName,
                    "MIRROR_JSON_PATH" to "$.examples[0]",
                    "MIRROR_JSON_PATH_TARGET" to Example::class.java.canonicalName
            )

            val store = WriteMirrorDataStoreImpl(injector)
            val partition = store.partition<String, ExampleHolder>("complex")

            it("saves complex objects") {
                partition["test"] = ExampleHolder(examples = listOf(Example("corge")))
            }
            it("loads complex objects") {
                partition["test"]?.examples.`should not be null`()
            }
            it("mirrors complex objects to mirror store") {
                val mirrorStore = TestDataStore.instances.first()
                val mirrorPartition = mirrorStore.partition<String, Example>("complex")

                // JsonPath should have pulled out the first element from the holder as a Map
                mirrorPartition["test"] `should be instance of` Example::class.java
                mirrorPartition["test"]?.original `should equal` "corge"
            }
        }
    }
})

data class Example(val original: String)

data class ExampleHolder(val examples: List<Example>)
