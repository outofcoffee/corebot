package com.gatehill.corebot.chat

import java.util.*

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatLines {
    private val generator by lazy { Random() }
    private fun Array<String>.chooseOne() = this[generator.nextInt(this.size)]

    fun pleaseWait() = arrayOf(
            "Just a min",
            "Hang on",
            "Hold on",
            "One moment",
            "One min",
            "Just a moment",
            "Give me a min"
    ).chooseOne()

    fun goodNews() = arrayOf(
            "Hooray :${goodNewsEmoji()}:",
            "Awesome :${goodNewsEmoji()}:",
            "Good news :${goodNewsEmoji()}:",
            "Sweet :${goodNewsEmoji()}:"
    ).chooseOne()

    fun goodNewsEmoji() = arrayOf(
            "raised_hands",
            "tada"
    ).chooseOne()

    fun badNews() = arrayOf(
            "Oh dear :${badNewsEmoji()}:",
            "Oh no :${badNewsEmoji()}:",
            "Bad news :${badNewsEmoji()}:"
    ).chooseOne()

    fun badNewsEmoji() = arrayOf(
            "cry",
            "scream"
    ).chooseOne()

    fun greeting() = arrayOf(
            "Hi",
            "Hello",
            "Hey"
    ).chooseOne()

    fun ready() = arrayOf(
            "Reporting for duty",
            "Ready for action"
    ).chooseOne()
}
