package com.gatehill.corebot.chat

import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.util.yamlMapper
import java.util.*

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ChatGenerator(dictionary: Map<String, List<String>>? = null) {
    private val dictionary: Map<String, List<String>>
    private val generator by lazy { Random() }

    init {
        @Suppress("UNCHECKED_CAST")
        this.dictionary = dictionary ?:
                Settings.chat.chatGenerator.use { yamlMapper.readValue(it, Map::class.java) }
                        as Map<String, List<String>>
    }

    private fun chooseOne(key: String): String = dictionary[key]?.let {
        val entry = it[generator.nextInt(it.size)]

        // e.g. ${goodNewsEmoji}
        "\\$\\{([a-zA-Z]+)}".toPattern().matcher(entry)
                ?.takeIf { it.find() }
                ?.let { it.replaceAll(chooseOne(it.group(1))) }
                ?: entry
    } ?: ""

    fun pleaseWait() = chooseOne("pleaseWait")
    fun goodNews() = chooseOne("goodNews")
    fun goodNewsEmoji() = chooseOne("goodNewsEmoji")
    fun badNews() = chooseOne("badNews")
    fun badNewsEmoji() = chooseOne("badNewsEmoji")
    fun greeting() = chooseOne("greeting")
    fun ready() = chooseOne("ready")
}
