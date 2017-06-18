package com.gatehill.corebot.backend.items

import com.gatehill.corebot.backend.items.action.ItemsActionDriverImpl
import com.gatehill.corebot.backend.items.action.factory.BorrowItemAsUserFactory
import com.gatehill.corebot.backend.items.action.factory.BorrowItemFactory
import com.gatehill.corebot.backend.items.action.factory.EvictItemFactory
import com.gatehill.corebot.backend.items.action.factory.EvictUserFromItemFactory
import com.gatehill.corebot.backend.items.action.factory.ReturnItemFactory
import com.gatehill.corebot.backend.items.action.factory.StatusAllFactory
import com.gatehill.corebot.backend.items.action.factory.StatusItemFactory
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.driver.ActionDriverFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ItemsDriverBootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory,
                                               factoryService: FactoryService,
                                               templateService: TemplateService) {
    init {
        // drivers
        actionDriverFactory.registerDriver("items", ItemsActionDriverImpl::class.java)

        // templates
        templateService.registerClasspathTemplateFile("/items-templates.yml")
        factoryService.registerFactory(BorrowItemFactory::class.java)
        factoryService.registerFactory(BorrowItemAsUserFactory::class.java)
        factoryService.registerFactory(ReturnItemFactory::class.java)
        factoryService.registerFactory(EvictItemFactory::class.java)
        factoryService.registerFactory(EvictUserFromItemFactory::class.java)
        factoryService.registerFactory(StatusItemFactory::class.java)
        factoryService.registerFactory(StatusAllFactory::class.java)
    }
}
