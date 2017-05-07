package com.gatehill.corebot.deploy

import com.gatehill.corebot.action.driver.ActionDriverFactory
import com.gatehill.corebot.chat.TemplateService
import com.gatehill.corebot.chat.model.template.*
import com.gatehill.corebot.driver.jenkins.action.JenkinsActionDriver
import com.gatehill.corebot.driver.rundeck.action.RundeckActionDriver
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bootstrap @Inject constructor(actionDriverFactory: ActionDriverFactory,
                                    templateService: TemplateService) {
    init {
        // default drivers
        actionDriverFactory.registerDriver("rundeck", RundeckActionDriver::class.java)
        actionDriverFactory.registerDriver("jenkins", JenkinsActionDriver::class.java)

        // built-in templates
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(LockActionTemplate::class.java)
        templateService.registerTemplate(UnlockActionTemplate::class.java)
        templateService.registerTemplate(StatusJobTemplate::class.java)
        templateService.registerTemplate(EnableJobTemplate::class.java)
        templateService.registerTemplate(DisableJobTemplate::class.java)
        templateService.registerTemplate(LockOptionTemplate::class.java)
        templateService.registerTemplate(UnlockOptionTemplate::class.java)
    }
}
