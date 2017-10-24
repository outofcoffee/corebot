package com.gatehill.corebot.bot

import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.operation.factory.ShowHelpFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BotBootstrap @Inject constructor(factoryService: FactoryService,
                                       templateService: TemplateService) {
    init {
        factoryService.registerFactory(ShowHelpFactory::class.java)
    }
}
