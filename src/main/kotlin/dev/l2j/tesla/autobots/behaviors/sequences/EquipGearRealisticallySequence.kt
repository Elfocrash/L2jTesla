package dev.l2j.tesla.autobots.behaviors.sequences

import dev.l2j.tesla.autobots.utils.CancellationToken
import dev.l2j.tesla.autobots.utils.giveItemsByClassAndLevel
import dev.l2j.tesla.gameserver.model.actor.Player

internal class EquipGearRealisticallySequence(override val bot: Player) :
    Sequence {

    override var cancellationToken: CancellationToken? = null

    override suspend fun definition() {
        giveItemsByClassAndLevel(bot, addRealistically = true)
    }
}