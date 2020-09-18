package dev.l2j.tesla.autobots.config

import dev.l2j.tesla.gameserver.model.location.Location

data class AutobotLocation(val x: Int, val y: Int, val z: Int, val location: Location = Location(
    x,
    y,
    z
)
)