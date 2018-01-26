package com.gatehill.corebot.plugin

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.gatehill.corebot.plugin.config.PluginSettings
import com.gatehill.corebot.plugin.model.PluginEnvironment
import com.gatehill.corebot.plugin.model.PluginWrapper
import com.gatehill.corebot.util.VersionUtil
import com.gatehill.corebot.util.yamlMapper
import com.gatehill.dlcl.Collector
import com.gatehill.dlcl.Downloader
import com.gatehill.dlcl.classloader.ChildFirstDownloadingClassLoader
import com.gatehill.dlcl.jcenter
import com.gatehill.dlcl.jitpack
import com.gatehill.dlcl.mavenCentral
import com.gatehill.dlcl.model.DependencyType
import com.google.inject.Module
import org.apache.logging.log4j.LogManager

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class PluginService {
    companion object {
        private const val PLUGIN_ENVIRONMENT_FILE = "plugin-environment.yml"
    }

    private val logger = LogManager.getLogger(PluginService::class.java)

    fun clearRepo() {
        Collector(PluginSettings.localRepo).clearCollected()
    }

    fun fetchPlugins() {
        val pluginEnvironment = loadPluginEnvironment()
        val repos = listRepos(pluginEnvironment)

        with(fetchPluginConfig()) {
            val allDependencies = frontends.map { it.dependency }
                    .union(backends.map { it.dependency })
                    .union(storage.map { it.dependency })

            logger.debug("Fetching ${allDependencies.size} plugins")

            allDependencies.forEach { dependency ->
                val coordinates = dependency.replace("\$VERSION", VersionUtil.version)
                val downloader = Downloader(PluginSettings.localRepo, null, emptyList(), repos)
                downloader.downloadSingleDependency(coordinates)
            }
        }
    }

    private fun loadPluginEnvironment() = yamlMapper.readValue<PluginEnvironment>(ClassLoaderUtil.classLoader.getResourceAsStream(
            PLUGIN_ENVIRONMENT_FILE), jacksonTypeRef<PluginEnvironment>())

    /**
     * Configured repositories take precedence.
     */
    private fun listRepos(pluginEnvironment: PluginEnvironment = loadPluginEnvironment()) =
            pluginEnvironment.repositories.toList().union(listOf(mavenCentral, jcenter, jitpack)).toList()

    fun instantiatePluginModules(): Collection<Module> {
        val classLoader = ChildFirstDownloadingClassLoader(
                PluginSettings.localRepo, listRepos(), PluginService::class.java.classLoader)

        // override the default classloader
        ClassLoaderUtil.classLoader = classLoader

        // load the already-downloaded classes
        classLoader.load(DependencyType.JAR)

        val pluginConfig = fetchPluginConfig()

        // frontends and backends are instantiated the same way
        val distinctClassNames = pluginConfig.frontends.union(pluginConfig.backends)
                .flatMap { (_, classes) -> classes }
                .distinct()

        logger.debug("Loading ${distinctClassNames.size} distinct front end and back end plugin modules")

        return distinctClassNames.map { className ->
            @Suppress("UNCHECKED_CAST")
            val moduleClass: Class<Module> = classLoader.loadClass(className) as Class<Module>
            moduleClass.newInstance()
        }
    }

    private fun fetchPluginConfig(): PluginWrapper =
            yamlMapper.readValue(PluginSettings.pluginsFile.toFile(), PluginWrapper::class.java)
}
