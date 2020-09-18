package dev.l2j.tesla.autobots.models

internal data class BotChat(val chatType: ChatType, val senderName: String, val message: String, val createdDate: Long = System.currentTimeMillis())