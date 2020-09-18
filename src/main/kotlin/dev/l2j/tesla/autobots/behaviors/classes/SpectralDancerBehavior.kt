package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.commons.random.Rnd
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class SpectralDancerBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(arrest) { _, _, target -> target is Player && !target.isRooted },
            BotSkill(judgement),
            BotSkill(demonicBladeDance){ player, _, target -> target is Player && distance(player, target) < 100 && Rnd.get(1, 10) < 4 }
            
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense) { player, _, _ -> player.getHealthPercentage() < 20 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}