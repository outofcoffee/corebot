package com.gatehill.corebot.driver.items

import com.gatehill.corebot.asSingleton
import com.gatehill.corebot.driver.items.service.ClaimService
import com.google.inject.AbstractModule

class ItemsDriverModule : AbstractModule() {
    override fun configure() {
        bind(ClaimService::class.java).asSingleton()
    }
}
