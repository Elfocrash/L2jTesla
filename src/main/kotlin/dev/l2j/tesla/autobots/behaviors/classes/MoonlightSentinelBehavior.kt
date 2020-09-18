package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.Kiter
import dev.l2j.tesla.autobots.behaviors.attributes.MiscItem
import dev.l2j.tesla.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.getArrowId
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class MoonlightSentinelBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    RequiresMiscItem,
    Kiter {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val miscItems = listOf(MiscItem({ b ->
        getArrowId(
            b
        )
    }))

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(101){_, _, target -> target is Player && target.pvpFlag > 0 },
            BotSkill(343){_, _, target -> target is Player },
            BotSkill(354){_, _, target -> target is Player },
            BotSkill(369){_, _, target -> target is Player })

    override val conditionalSkills: List<Int> = emptyList()
}