package com.gatehill.corebot

import com.gatehill.corebot.action.factory.LockActionFactory
import com.gatehill.corebot.action.factory.LockOptionFactory
import com.gatehill.corebot.action.factory.ShowHelpFactory
import com.gatehill.corebot.action.factory.StatusActionFactory
import com.gatehill.corebot.action.factory.StatusOptionFactory
import com.gatehill.corebot.action.factory.UnlockActionFactory
import com.gatehill.corebot.action.factory.UnlockOptionFactory
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.driver.ActionDriverFactory
import com.gatehill.corebot.driver.jenkins.action.JenkinsActionDriver
import com.gatehill.corebot.driver.jobs.action.factory.DisableJobFactory
import com.gatehill.corebot.driver.jobs.action.factory.EnableJobFactory
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory,
                                    factoryService: FactoryService,
                                    templateService: TemplateService) {
    init {
        // drivers
        actionDriverFactory.registerDriver("rundeck", RundeckActionDriver::class.java)
        actionDriverFactory.registerDriver("jenkins", JenkinsActionDriver::class.java)

        // templates
        templateService.registerClasspathTemplateFile("/jobs-templates.yml")
        factoryService.registerFactory(ShowHelpFactory::class.java)
        factoryService.registerFactory(LockActionFactory::class.java)
        factoryService.registerFactory(UnlockActionFactory::class.java)
        factoryService.registerFactory(StatusActionFactory::class.java)
        factoryService.registerFactory(EnableJobFactory::class.java)
        factoryService.registerFactory(DisableJobFactory::class.java)
        factoryService.registerFactory(LockOptionFactory::class.java)
        factoryService.registerFactory(UnlockOptionFactory::class.java)
        factoryService.registerFactory(StatusOptionFactory::class.java)
    }
}
