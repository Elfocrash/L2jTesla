package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.Healer
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.extensions.getClosestEntityInRadius
import dev.l2j.tesla.autobots.extensions.useMagicSkill
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.getHealthPercentage
import dev.l2j.tesla.gameserver.enums.IntentionType
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.L2Skill
import dev.l2j.tesla.gameserver.model.actor.Creature
import dev.l2j.tesla.gameserver.model.actor.Player


internal class CardinalBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    Healer {
    
    override var skillPreferences: SkillPreferences =
        SkillPreferences(true)

    override val conditionalSkills: List<Int> = emptyList()
    
    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }

    override suspend fun afterAttack() {
        super.afterAttack()

        if(player.target == player && player.getHealthPercentage() > 80) {
            player.target = null
        }
        
        if(player.target == null) {
            if(player.isInParty) {
                player.target = player.party.leader
                player.ai.setIntention(IntentionType.FOLLOW, player.target)
                return
            }

            if(player.clan != null) {
                val closestClanMember =  player.getClosestEntityInRadius<Player>(2000) { it is Player && it.clan != null && it.clanId == player.clanId}
                player.target = closestClanMember
                player.ai.setIntention(IntentionType.FOLLOW, player.target)
            }
        }
    }

    override suspend fun beforeAttack() {
        super.beforeAttack()
        if(player.target == null) {
            
        }
    }

    override suspend fun attack() {
        heal()
    }

    override suspend fun targetAppropriateTarget() {
        tryTargetingLowestHpTargetInRadius(player, 2000)
    }

    private fun heal() {        
        val skill = getNextAvailableHealingSkill()
        val target = player.target
        
        if(target != null && skill != null) {
            player.useMagicSkill(skill)
        }
    }

    override val getOffensiveSkills: List<BotSkill> = emptyList()
    
    val healingSkills: List<BotSkill> = listOf(
            BotSkill(1218) { player, _, target -> target != null && target is Player && (player.getHealthPercentage()) < 70 },
            BotSkill(1217) { player, _, target -> target != null && target is Player && (player.getHealthPercentage()) < 80 })

    fun getNextAvailableHealingSkill(): L2Skill? {

        if(healingSkills.isEmpty()) return null

        var skillToUse: L2Skill? = null
        for(botSkill in healingSkills) {
            val skill = player.getSkill(botSkill.skillId)
            if(!player.checkDoCastConditions(skill))
                continue

            if(botSkill.condition(player, skill, player.target as Creature?) && skill.checkCondition(player, player.target, false)) {
                skillToUse = skill
                break
            }
        }

        if(skillToUse == null) return null

        return skillToUse
    }
}