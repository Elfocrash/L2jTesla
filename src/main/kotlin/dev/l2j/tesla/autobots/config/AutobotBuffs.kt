package dev.l2j.tesla.autobots.config

import com.fasterxml.jackson.annotation.JsonProperty
import dev.l2j.tesla.gameserver.enums.actors.ClassId

data class AutobotBuffs(@JsonProperty("classid") val classId: ClassId, @JsonProperty("buffs")val buffsAsString: String){
    val buffsContent : Array<IntArray> = buffsAsString.split(";").map { intArrayOf(it.split(",")[0].toInt(), it.split(",")[1].toInt()) }.toTypedArray()
}