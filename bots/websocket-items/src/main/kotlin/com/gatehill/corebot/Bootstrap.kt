package com.gatehill.corebot

import com.gatehill.corebot.action.factory.ShowHelpFactory
import com.gatehill.corebot.action.factory.SetRealNameFactory
import com.gatehill.corebot.action.factory.SetUsernameFactory
import com.gatehill.corebot.chat.template.TemplateConfigService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.driver.ActionDriverFactory
import com.gatehill.corebot.driver.items.action.ItemsActionDriverImpl
import com.gatehill.corebot.driver.items.action.factory.BorrowItemAsUserFactory
import com.gatehill.corebot.driver.items.action.factory.BorrowItemFactory
import com.gatehill.corebot.driver.items.action.factory.EvictItemFactory
import com.gatehill.corebot.driver.items.action.factory.ReturnItemFactory
import com.gatehill.corebot.driver.items.action.factory.StatusAllFactory
import com.gatehill.corebot.driver.items.action.factory.StatusItemFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory,
                                    templateService: TemplateService,
                                    templateConfigService: TemplateConfigService) {
    init {
        // drivers
        actionDriverFactory.registerDriver("items", ItemsActionDriverImpl::class.java)

        // templates
        templateConfigService.registerClasspathTemplateFile("/items-templates.yml")
        templateConfigService.registerClasspathTemplateFile("/websocket-templates.yml")
        templateService.registerTemplate(ShowHelpFactory::class.java)
        templateService.registerTemplate(BorrowItemFactory::class.java)
        templateService.registerTemplate(BorrowItemAsUserFactory::class.java)
        templateService.registerTemplate(ReturnItemFactory::class.java)
        templateService.registerTemplate(EvictItemFactory::class.java)
        templateService.registerTemplate(StatusItemFactory::class.java)
        templateService.registerTemplate(StatusAllFactory::class.java)

        templateService.registerTemplate(SetUsernameFactory::class.java)
        templateService.registerTemplate(SetRealNameFactory::class.java)
    }
}
