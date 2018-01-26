package com.gatehill.corebot.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Converts YAML to objects.
 */
val yamlMapper by lazy { YAMLMapper().registerKotlinModule() }

/**
 * Converts JSON to objects.
 */
val jsonMapper by lazy { ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS).registerKotlinModule() }
