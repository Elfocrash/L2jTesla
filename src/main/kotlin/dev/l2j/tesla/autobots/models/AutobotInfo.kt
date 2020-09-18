package dev.l2j.tesla.autobots.models

import dev.l2j.tesla.gameserver.enums.actors.ClassId

internal data class AutobotInfo(val name: String, val level:Int, val isOnline: Boolean, val classId: ClassId, val botId: Int, val clanId: Int)

internal data class ScheduledSpawnInfo(val botName: String, val loginTime: String, val logoutTime: String)