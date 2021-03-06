package dev.l2j.tesla.autobots.behaviors.classes.pre

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.distance
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class DarkElvenWizardBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    SecretManaRegen {
    
    override var skillPreferences: SkillPreferences =
        SkillPreferences(true)

    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(1266),
            BotSkill(1172){player, _, target -> target != null && distance(player, target) < 200 },
            BotSkill(1178),
            BotSkill(1175))

    override val conditionalSkills: List<Int> = emptyList()
}