package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.getHealthPercentage
import dev.l2j.tesla.commons.random.Rnd
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class TitanBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(362),
            BotSkill(315) { _, _, _ -> Rnd.nextDouble() < 0.8 },
            BotSkill(190) { _, _, _ -> Rnd.nextDouble() < 0.2 }            )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(139) { player, _, _ -> player.getHealthPercentage() < 30 },
            BotSkill(176) { player, _, _ -> player.getHealthPercentage() < 15 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}