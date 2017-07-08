package com.gatehill.corebot

import com.gatehill.corebot.action.factory.LockActionFactory
import com.gatehill.corebot.action.factory.LockOptionFactory
import com.gatehill.corebot.action.factory.ShowHelpFactory
import com.gatehill.corebot.action.factory.StatusActionFactory
import com.gatehill.corebot.action.factory.UnlockActionFactory
import com.gatehill.corebot.action.factory.UnlockOptionFactory
import com.gatehill.corebot.chat.template.TemplateConfigService
import com.gatehill.corebot.chat.template.TemplateService
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
                                    templateService: TemplateService,
                                    templateConfigService: TemplateConfigService) {
    init {
        // drivers
        actionDriverFactory.registerDriver("rundeck", RundeckActionDriver::class.java)
        actionDriverFactory.registerDriver("jenkins", JenkinsActionDriver::class.java)

        // templates
        templateConfigService.registerClasspathTemplateFile("/jobs-templates.yml")
        templateService.registerTemplate(ShowHelpFactory::class.java)
        templateService.registerTemplate(LockActionFactory::class.java)
        templateService.registerTemplate(UnlockActionFactory::class.java)
        templateService.registerTemplate(StatusActionFactory::class.java)
        templateService.registerTemplate(EnableJobFactory::class.java)
        templateService.registerTemplate(DisableJobFactory::class.java)
        templateService.registerTemplate(LockOptionFactory::class.java)
        templateService.registerTemplate(UnlockOptionFactory::class.java)
    }
}
