package dev.l2j.tesla.autobots.behaviors.attributes

import dev.l2j.tesla.autobots.Autobot
import dev.l2j.tesla.autobots.extensions.useItem
import dev.l2j.tesla.gameserver.model.actor.Player

internal interface ConsumableUser {
    val consumables: List<Consumable>
    
    fun handleConsumables(player: Player){
        if(consumables.isEmpty()) return

        consumables.forEach {
            
            if(!it.condition(player)) return@forEach
            
            val items = player.inventory.getItemsByItemId(it.consumableId)
            
            if(items.isEmpty() && player is Autobot) {
                player.addItem("AubotoItem", it.consumableId, 200, player, false)
            }
            
            player.useItem(it.consumableId)
        }
    }
}
internal data class Consumable(val consumableId: Int, val condition: (Player) -> Boolean = { true })