package com.gatehill.corebot

import com.gatehill.corebot.chat.parser.RegexParser
import com.gatehill.corebot.chat.parser.StringParser
import com.gatehill.corebot.chat.template.TemplateConfigService
import com.google.inject.AbstractModule

/**
 * Core bindings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class CoreModule : AbstractModule() {
    override fun configure() {
        bind(TemplateConfigService::class.java).asSingleton()
        bind(StringParser::class.java).asSingleton()
        bind(RegexParser::class.java).asSingleton()
    }
}
