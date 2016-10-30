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
            "Hooray :raised_hands:",
            "Awesome :raised_hands:",
            "Good news :raised_hands:"
    ).chooseOne()

    fun badNews() = arrayOf(
            "Oh dear :cry:",
            "Oh no :cry:",
            "Bad news :cry:"
    ).chooseOne()
}
