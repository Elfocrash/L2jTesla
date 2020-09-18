package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class MysticMuseBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(true)
    
    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(1265),
            BotSkill(1342),
            BotSkill(1340),
            BotSkill(1235))

    override val conditionalSkills: List<Int> = emptyList()
}