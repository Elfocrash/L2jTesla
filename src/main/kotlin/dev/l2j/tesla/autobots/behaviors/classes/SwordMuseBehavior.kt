package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.skills.SwordMuseSkillPreferences
import dev.l2j.tesla.autobots.extensions.getCombatBehavior
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class SwordMuseBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {
    
    override var skillPreferences: SkillPreferences =
        SwordMuseSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(arrest) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(songOfSilence){player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && player.getCombatBehavior().validateConditionalSkill(skill) && target != null && !target.hasEffect(songOfSilence) }
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(deflectArrow, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(holyBlade, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
    )

    override val conditionalSkills: List<Int> = listOf(ultimateDefense, songOfSilence)
}