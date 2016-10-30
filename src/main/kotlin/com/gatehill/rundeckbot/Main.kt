package com.gatehill.rundeckbot

import com.gatehill.rundeckbot.chat.ChatService

fun main(args: Array<String>) {
    ChatService.listenForEvents()
}
