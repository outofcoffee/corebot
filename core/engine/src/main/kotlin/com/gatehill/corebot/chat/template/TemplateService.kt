package com.gatehill.corebot.chat.template

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.gatehill.corebot.operation.factory.OperationFactory
import com.gatehill.corebot.operation.factory.readOperationFactoryMetadata
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.chat.filter.RegexFilter
import com.gatehill.corebot.chat.filter.StringFilter
import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.gatehill.corebot.util.yamlMapper
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TemplateService {
    private data class RawTemplateConfig(val template: String,
                                         val usage: String?)

    private val classpathPrefix = "classpath:"
    private val regexTemplatePattern = Pattern.compile("/(?<template>.+)/")
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
                val classpathFile = it.substring(classpathPrefix.length + if (it.startsWith("$classpathPrefix/")) 1 else 0)
                ClassLoaderUtil.classLoader.getResourceAsStream(classpathFile)
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

    fun loadFilterConfig(templateName: String): List<FilterConfig> =
            allConfigs.filterKeys { it == templateName }.values.flatMap { config ->
                config.map {
                    val templateMatcher = regexTemplatePattern.matcher(it.template)
                    if (templateMatcher.matches()) {
                        RegexFilter.RegexFilterConfig(
                                template = Pattern.compile(templateMatcher.group("template")),
                                usage = it.usage
                        )
                    } else {
                        StringFilter.StringFilterConfig(
                                template = it.template,
                                usage = it.usage ?: it.template
                        )
                    }
                }
            }

    fun loadFilterConfig(factoryClass: Class<out OperationFactory>) =
            loadFilterConfig(readOperationFactoryMetadata(factoryClass).templateName)

    fun registerClasspathTemplateFile(classpathFile: String) {
        templateFiles += "$classpathPrefix$classpathFile"
    }

    fun registerFilesystemTemplateFile(file: Path) {
        templateFiles += file.toAbsolutePath().toString()
    }
}
