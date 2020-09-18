package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class GhostHunterBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(bluff) { _, _, target -> target !is Player || (target.pvpFlag > 0 || target.karma > 0)  },
            BotSkill(backstab) { player, _, _ -> player.isBehindTarget },
            BotSkill(lethalBlow),
            BotSkill(deadlyBlow),
            BotSkill(mortalStrike),
            BotSkill(blindingBlow))

    override val conditionalSkills: List<Int> = emptyList()
}