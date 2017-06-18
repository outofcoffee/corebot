package com.gatehill.corebot.backend.jobs

import com.gatehill.corebot.operation.factory.LockActionFactory
import com.gatehill.corebot.operation.factory.LockOptionFactory
import com.gatehill.corebot.operation.factory.StatusActionFactory
import com.gatehill.corebot.operation.factory.UnlockActionFactory
import com.gatehill.corebot.operation.factory.UnlockOptionFactory
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.backend.jobs.action.factory.DisableJobFactory
import com.gatehill.corebot.backend.jobs.action.factory.EnableJobFactory
import com.gatehill.corebot.operation.factory.StatusOptionFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class JobsDriverBootstrap @Inject constructor(factoryService: FactoryService,
                                              templateService: TemplateService) {
    init {
        templateService.registerClasspathTemplateFile("/jobs-templates.yml")
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
