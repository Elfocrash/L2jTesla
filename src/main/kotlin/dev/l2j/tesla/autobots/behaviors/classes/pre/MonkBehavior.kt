package dev.l2j.tesla.autobots.behaviors.classes.pre

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class MonkBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    SecretManaRegen {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)

    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(120){ _, _, target -> target is Player && !target.isStunned},
            BotSkill(54){ player, skill, _ -> player.charges < skill.maxCharges},
            BotSkill(284){ player, skill, _ -> player.charges < skill.maxCharges})

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(50){player, skill, _ ->  player.charges < skill.maxCharges }
    )

    override val conditionalSkills: List<Int> = emptyList()
}