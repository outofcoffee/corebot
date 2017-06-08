package com.gatehill.corebot

import com.gatehill.corebot.action.driver.ActionDriverFactory
import com.gatehill.corebot.chat.TemplateService
import com.gatehill.corebot.chat.model.template.ShowHelpTemplate
import com.gatehill.corebot.driver.items.action.ItemsActionDriverImpl
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemAsUserTemplate
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemTemplate
import com.gatehill.corebot.driver.items.chat.model.template.EvictItemTemplate
import com.gatehill.corebot.driver.items.chat.model.template.EvictUserFromItemTemplate
import com.gatehill.corebot.driver.items.chat.model.template.ReturnItemTemplate
import com.gatehill.corebot.driver.items.chat.model.template.StatusAllTemplate
import com.gatehill.corebot.driver.items.chat.model.template.StatusItemTemplate
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory,
                                    templateService: TemplateService) {
    init {
        // drivers
        actionDriverFactory.registerDriver("items", ItemsActionDriverImpl::class.java)

        // templates
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(BorrowItemTemplate::class.java)
        templateService.registerTemplate(BorrowItemAsUserTemplate::class.java)
        templateService.registerTemplate(ReturnItemTemplate::class.java)
        templateService.registerTemplate(EvictItemTemplate::class.java)
        templateService.registerTemplate(EvictUserFromItemTemplate::class.java)
        templateService.registerTemplate(StatusItemTemplate::class.java)
        templateService.registerTemplate(StatusAllTemplate::class.java)
    }
}
