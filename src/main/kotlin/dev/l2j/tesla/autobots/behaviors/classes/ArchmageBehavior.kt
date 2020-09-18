package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.skills.ArchmageSkillPreferences
import dev.l2j.tesla.autobots.extensions.getCombatBehavior
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player
import dev.l2j.tesla.gameserver.model.actor.instance.Monster


internal class ArchmageBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        ArchmageSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(surrenderToFire) {_, _, target -> skillPreferences.togglableSkills[surrenderToFire]!! &&  ((target is Monster) || (target is Player && (target.pvpFlag > 0 || target.karma > 0))) && !target.hasEffect(surrenderToFire) },
            BotSkill(cancellation) {_, _, target -> skillPreferences.togglableSkills[cancellation]!! && target is Player && (target.pvpFlag > 0 || target.karma > 0) && target.allEffects.size > 5 },            
            BotSkill(fireVortex),
            BotSkill(slow) { player, skill, target -> target != null && !target.hasEffect(slow) && player.getCombatBehavior().validateConditionalSkill(skill)},
            BotSkill(prominence)
    )

    override val getSelfSupportSkills: List<BotSkill> =
            listOf(BotSkill(arcanePower, { player, _, _ -> player.getCombatBehavior().skillPreferences.togglableSkills[arcanePower]!! && !player.hasEffect(arcanePower) && player.isInCombat }, true))

    override val conditionalSkills: List<Int> = listOf(slow)
}