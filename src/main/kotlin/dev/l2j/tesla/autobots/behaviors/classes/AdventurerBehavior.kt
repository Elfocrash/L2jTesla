package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.skills.AdventurerSkillPreferences
import dev.l2j.tesla.autobots.extensions.getCombatBehavior
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player


internal class AdventurerBehavior(player: Player, combatPreferences: CombatPreferences) :
    CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences =
        AdventurerSkillPreferences()

    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
        BotSkill(bluff) { _, _, target -> target is Player && (target.pvpFlag > 0 || target.karma > 0) },
        BotSkill(trick) { player, _, target -> target is Player && (target.pvpFlag > 0 || target.karma > 0) && player.getCombatBehavior().skillPreferences.togglableSkills[trick]!! },
        BotSkill(switch) { player, _, target -> target is Player && (target.pvpFlag > 0 || target.karma > 0) && player.getCombatBehavior().skillPreferences.togglableSkills[switch]!! },
        BotSkill(backstab) { player, _, _ -> player.isBehindTarget },
        BotSkill(lethalBlow),
        BotSkill(deadlyBlow),
        BotSkill(criticalBlow, isConditionalSkill = false, isTogglableSkill = false, useWhenEffectIsNotPresent = true)
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
        BotSkill(dash, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
        BotSkill(
            ultimateEvasion,
            isConditionalSkill = true,
            isTogglableSkill = false,
            useWhenEffectIsNotPresent = false
        ),
        BotSkill(mirage, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
        BotSkill(viciousStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
        BotSkill(focusPower, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
        BotSkill(
            focusSkillMastery,
            isConditionalSkill = false,
            isTogglableSkill = true,
            useWhenEffectIsNotPresent = true
        )
    )

    override val conditionalSkills: List<Int> = listOf(dash, ultimateEvasion, mirage)
}