package dev.l2j.tesla.autobots.behaviors.classes.pre

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.getHealthPercentage
import dev.l2j.tesla.autobots.utils.shieldStun
import dev.l2j.tesla.autobots.utils.ultimateDefense
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class KnightBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    SecretManaRegen {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)

    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(shieldStun) { _, _, target -> target is Player && !target.isStunned })

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense) { player, _, _ -> player.getHealthPercentage() < 40 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}