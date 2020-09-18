package dev.l2j.tesla.autobots.config

import com.fasterxml.jackson.annotation.JsonProperty
import dev.l2j.tesla.gameserver.enums.actors.ClassId

data class AutobotSymbol(@JsonProperty("classId") val classId: ClassId, @JsonProperty("symbols")val symbols: String){
    val symbolIds : Array<Int> = symbols.split(";").map { it.toInt() }.toTypedArray()
}