package com.gatehill.corebot.bot

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.google.inject.AbstractModule
import com.google.inject.Guice.createInjector
import com.google.inject.Module
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bot @Inject constructor(private val chatService: ChatService) {
    /**
     * The main entrypoint.
     */
    fun start() {
        chatService.listenForEvents()

        if (chatService.supportsUserTermination) {
            try {
                val reader = BufferedReader(InputStreamReader(System.`in`))
                println("Press <ENTER> to terminate.")
                reader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        println("Terminating...")
        chatService.stopListening()
        println("Bye!")
        System.exit(0)
    }

    /**
     * Constructs `Bot` instances.
     */
    companion object Builder {
        private val logger: Logger = LogManager.getLogger(Builder::class.java)

        private val coreModules = listOf(
                "com.gatehill.corebot.CoreModule",
                "com.gatehill.corebot.bot.CommonBotModule"
        )

        /**
         * Construct a new `Bot` and wire up its dependencies.
         */
        fun build(vararg extensionModules: Module): Bot {
            val modules = mutableListOf<Module>().apply {
                addAll(coreModules.map { ClassLoaderUtil.loadClass<Module>(it).newInstance() })
                add(object : AbstractModule() {
                    override fun configure() {
                        extensionModules.forEach {
                            logger.debug("Installing injection module: ${it.javaClass.canonicalName}")
                            install(it)
                        }
                    }
                })
            }

            return createInjector(modules).getInstance(Bot::class.java)
        }
    }
}
