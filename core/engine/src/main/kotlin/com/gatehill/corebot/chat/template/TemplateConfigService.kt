package com.gatehill.corebot.chat.template

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.parser.ParserConfig
import com.gatehill.corebot.chat.parser.RegexParser
import com.gatehill.corebot.chat.parser.StringParser
import com.gatehill.corebot.util.yamlMapper
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateConfigService {
    private data class RawTemplateConfig(val template: String,
                                         val usage: String?)

    private val classpathPrefix = "classpath:"
    private val templateFiles = mutableListOf<String>()

    init {
        registerClasspathTemplateFile("/core-templates.yml")
    }

    /**
     * Return a consolidated `Map` of configurations from all `templateFiles`.
     */
    private val allConfigs: Map<String, Collection<RawTemplateConfig>> by lazy {
        val configMap = mutableMapOf<String, Collection<RawTemplateConfig>>()

        templateFiles.forEach {
            if (it.startsWith(classpathPrefix)) {
                TemplateConfigService::class.java.getResourceAsStream(it.substring(classpathPrefix.length))
            } else {
                Paths.get(it).toFile().inputStream()
            }.use {
                readAndMerge(configMap, it)
            }
        }

        configMap
    }

    private fun readAndMerge(configMap: MutableMap<String, Collection<RawTemplateConfig>>, fileStream: InputStream) {
        readTemplateFile(fileStream)?.forEach { (key, value) ->
            configMap.merge(key, value) { value1, value2 ->
                value1.union(value2)
            }
        }
    }

    private fun readTemplateFile(fileStream: InputStream): Map<String, List<RawTemplateConfig>>? =
            yamlMapper.readValue<Map<String, List<RawTemplateConfig>>>(fileStream,
                    jacksonTypeRef<Map<String, List<RawTemplateConfig>>>())

    fun loadParserConfig(templateName: String): List<ParserConfig> =
            allConfigs.filterKeys { it == templateName }.values.flatMap { config ->
                config.map {
                    // TODO use regex instead
                    if (it.template.startsWith("/") && it.template.endsWith("/")) {
                        RegexParser.RegexParserConfig(
                                template = Pattern.compile(it.template.substring(1, it.template.length - 1)),
                                usage = it.usage
                        )
                    } else {
                        StringParser.StringParserConfig(
                                template = it.template,
                                usage = it.usage ?: it.template
                        )
                    }
                }
            }

    fun loadParserConfig(templateClass: Class<out ActionTemplate>) =
            loadParserConfig(templateClass.simpleName)

    fun registerClasspathTemplateFile(classpathFile: String) {
        templateFiles += "$classpathPrefix$classpathFile"
    }

    fun registerFilesystemTemplateFile(file: Path) {
        templateFiles += file.toAbsolutePath().toString()
    }
}
