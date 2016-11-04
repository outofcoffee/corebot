package com.gatehill.rundeckbot.chat

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatLines {
    private fun Array<String>.chooseOne() = this[(Math.random() * (this.size - 1)).toInt()]

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
            "Good news :${goodNewsEmoji()}:"
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
}
