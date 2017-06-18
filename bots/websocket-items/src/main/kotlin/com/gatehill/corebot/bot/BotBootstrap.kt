package com.gatehill.corebot.bot

import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.frontend.websocket.operation.factory.SetRealNameFactory
import com.gatehill.corebot.frontend.websocket.operation.factory.SetUsernameFactory
import com.gatehill.corebot.frontend.websocket.operation.factory.TerminateSessionFactory
import com.gatehill.corebot.operation.factory.ShowHelpFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BotBootstrap @Inject constructor(factoryService: FactoryService,
                                       templateService: TemplateService) {
    init {
        // core templates
        factoryService.registerFactory(ShowHelpFactory::class.java)

        // ws specific templates
        templateService.registerClasspathTemplateFile("/websocket-templates.yml")
        factoryService.registerFactory(SetUsernameFactory::class.java)
        factoryService.registerFactory(SetRealNameFactory::class.java)
        factoryService.registerFactory(TerminateSessionFactory::class.java)
    }
}
