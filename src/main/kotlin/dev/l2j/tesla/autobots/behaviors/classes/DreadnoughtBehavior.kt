package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.skills.DreadnoughtSkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class DreadnoughtBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        DreadnoughtSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(361),
            BotSkill(347){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(48){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(452){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(36){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 })

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(braveheart, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(revival, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(battleroar, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(fellSwoop, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(viciousStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(warFrenzy, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(thrillFight, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
            
    )

    override val conditionalSkills: List<Int> = listOf(braveheart,revival,battleroar)
}