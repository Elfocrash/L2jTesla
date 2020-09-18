package dev.l2j.tesla.autobots.behaviors.attributes

import dev.l2j.tesla.autobots.Autobot
import dev.l2j.tesla.autobots.utils.getManaPercentage
import dev.l2j.tesla.gameserver.model.actor.Player

interface SecretManaRegen {
    fun regenMana(autobot: Player){
        if(autobot.getManaPercentage() < 20 && autobot is Autobot) {
            autobot.currentMp = autobot.maxMp.toDouble()
        }
    }
}