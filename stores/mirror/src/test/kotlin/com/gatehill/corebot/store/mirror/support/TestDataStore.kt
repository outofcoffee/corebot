package com.gatehill.corebot.store.mirror.support

import com.gatehill.corebot.store.InMemoryDataStoreImpl

class TestDataStore : InMemoryDataStoreImpl() {
    init {
        instances += this
    }

    companion object {
        val instances = mutableListOf<TestDataStore>()
    }
}
