package com.gatehill.corebot.chat

import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.util.yamlMapper
import java.util.Random

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ChatGeneratorImpl(dictionary: Map<String, List<String>>? = null) : ChatGenerator {
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

    override fun pleaseWait() = chooseOne("pleaseWait")
    override fun goodNews() = chooseOne("goodNews")
    override fun goodNewsEmoji() = chooseOne("goodNewsEmoji")
    override fun badNews() = chooseOne("badNews")
    override fun badNewsEmoji() = chooseOne("badNewsEmoji")
    override fun greeting() = chooseOne("greeting")
    override fun ready() = chooseOne("ready")
    override fun confirmation() = chooseOne("confirmation")
}
