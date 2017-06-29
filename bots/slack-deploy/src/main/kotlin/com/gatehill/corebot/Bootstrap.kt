package com.gatehill.corebot

import com.gatehill.corebot.action.driver.ActionDriverFactory
import com.gatehill.corebot.chat.model.template.DisableJobTemplate
import com.gatehill.corebot.chat.model.template.EnableJobTemplate
import com.gatehill.corebot.chat.model.template.LockActionTemplate
import com.gatehill.corebot.chat.model.template.LockOptionTemplate
import com.gatehill.corebot.chat.model.template.ShowHelpTemplate
import com.gatehill.corebot.chat.model.template.StatusActionTemplate
import com.gatehill.corebot.chat.model.template.UnlockActionTemplate
import com.gatehill.corebot.chat.model.template.UnlockOptionTemplate
import com.gatehill.corebot.chat.template.TemplateConfigService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.driver.jenkins.action.JenkinsActionDriver
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
        templateService.registerTemplate(ShowHelpTemplate::class.java)
        templateService.registerTemplate(LockActionTemplate::class.java)
        templateService.registerTemplate(UnlockActionTemplate::class.java)
        templateService.registerTemplate(StatusActionTemplate::class.java)
        templateService.registerTemplate(EnableJobTemplate::class.java)
        templateService.registerTemplate(DisableJobTemplate::class.java)
        templateService.registerTemplate(LockOptionTemplate::class.java)
        templateService.registerTemplate(UnlockOptionTemplate::class.java)
    }
}
