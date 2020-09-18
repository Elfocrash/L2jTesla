package dev.l2j.tesla.autobots.behaviors.classes

import dev.l2j.tesla.autobots.behaviors.CombatBehavior
import dev.l2j.tesla.autobots.behaviors.attributes.MiscItem
import dev.l2j.tesla.autobots.behaviors.attributes.PetOwner
import dev.l2j.tesla.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.tesla.autobots.skills.BotSkill
import dev.l2j.tesla.autobots.utils.*
import dev.l2j.tesla.gameserver.enums.items.ShotType
import dev.l2j.tesla.gameserver.model.actor.Player
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor


internal class MaestroBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences),
    PetOwner {

    override var skillPreferences: SkillPreferences =
        SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override suspend fun beforeAttack() {
        super.beforeAttack()

        if(player.hasServitor()) {
            val servitor = player.summon as Servitor
            servitor.applyBuffs(*getDefaultFighterBuffs())
        }
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(hammerCrush){_, _, target -> target is Player && !target.isStunned},
            BotSkill(stunAttack){_, _, target -> target is Player && !target.isStunned},
            BotSkill(armorCrush)
    )
    
    override val summonInfo: Pair<BotSkill, MiscItem?> = Pair(BotSkill(25) { player, _, _ -> !player.isInCombat },
        MiscItem({ 1459 }, 50, 10)
    )

    override val petBuffs: Array<IntArray> = getDefaultFighterBuffs()
    
    override val conditionalSkills: List<Int> = emptyList()
}