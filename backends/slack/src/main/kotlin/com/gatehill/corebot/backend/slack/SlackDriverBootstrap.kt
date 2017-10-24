package com.gatehill.corebot.backend.slack

import com.gatehill.corebot.backend.slack.action.factory.ForwardMessageFactory
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.chat.template.TemplateService
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackDriverBootstrap @Inject constructor(factoryService: FactoryService,
                                               templateService: TemplateService) {
    init {
        // templates
        templateService.registerClasspathTemplateFile("/slack-templates.yml")
        factoryService.registerFactory(ForwardMessageFactory::class.java)
    }
}
