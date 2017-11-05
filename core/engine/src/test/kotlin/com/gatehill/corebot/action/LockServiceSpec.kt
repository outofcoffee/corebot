package com.gatehill.corebot.action

import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.store.DataStore
import com.nhaarman.mockito_kotlin.mock
import org.amshove.kluent.VerifyNoFurtherInteractions
import org.amshove.kluent.`should be null`
import org.amshove.kluent.on
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


/**
 * Specification for `LockService`.
 */
object LockServiceSpec : Spek({
    given("a lock service") {
        on("passing an empty options map") {
            val lockStore = mock<DataStore>()
            val chatGenerator = mock<ChatGenerator>()
            val service = LockService(lockStore, chatGenerator)

            val lock = service.findOptionLock(emptyMap())

            it("returns null") {
                lock.`should be null`()
                VerifyNoFurtherInteractions on lockStore
            }
        }
    }
})
