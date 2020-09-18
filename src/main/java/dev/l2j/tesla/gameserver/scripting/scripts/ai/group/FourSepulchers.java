package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.FourSepulchersManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.location.Location;

public class FourSepulchers extends L2AttackableAIScript
{
	private static final String QUEST_ID = "Q620_FourGoblets";
	
	private static final int ANTIQUE_BROOCH = 7262;
	
	public FourSepulchers()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157);
		addKillId(18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131, 18149, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18165, 18183, 18184, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219, 18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157, 18141, 18142, 18143, 18144, 18145, 18146, 18147, 18148, 18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 25339, 25342, 25346, 25349);
		addSpawnId(18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243, 25339, 25342, 25346, 25349);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("safety"))
		{
			if (!npc.isDead() && npc.isVisible())
			{
				FourSepulchersManager.getInstance().spawnKeyBox(npc);
				npc.broadcastNpcSay("Thank you for saving me.");
				npc.deleteMe();
			}
		}
		else if (event.equalsIgnoreCase("aggro"))
		{
			// Aggro a single Imperial Guard.
			for (Attackable guard : npc.getKnownTypeInRadius(Attackable.class, 600))
			{
				switch (guard.getNpcId())
				{
					case 18166:
					case 18167:
					case 18168:
					case 18169:
						attack(guard, npc);
						return null;
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Attackable)
		{
			// Calculate random coords.
			final int rndX = npc.getX() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
			final int rndY = npc.getY() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
			
			// Wait the NPC to be immobile to move him again. Also check destination point.
			if (!npc.isMoving() && GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), rndX, rndY, npc.getZ()))
			{
				// Set the NPC as running.
				npc.setRunning();
				
				// Move the NPC.
				npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(rndX, rndY, npc.getZ()));
				
				// 50% to call a specific player. If no player can be found, we use generic string.
				Player playerToCall = null;
				if (Rnd.nextBoolean())
					playerToCall = Rnd.get(npc.getKnownTypeInRadius(Player.class, 1200));
				
				npc.broadcastNpcSay((playerToCall == null) ? "Help me!!" : "%s! Help me!!".replaceAll("%s", playerToCall.getName()));
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		switch (npc.getNpcId())
		{
			case 18120:
			case 18121:
			case 18122:
			case 18123:
			case 18124:
			case 18125:
			case 18126:
			case 18127:
			case 18128:
			case 18129:
			case 18130:
			case 18131:
			case 18149:
			case 18158:
			case 18159:
			case 18160:
			case 18161:
			case 18162:
			case 18163:
			case 18164:
			case 18165:
			case 18183:
			case 18184:
			case 18212:
			case 18213:
			case 18214:
			case 18215:
			case 18216:
			case 18217:
			case 18218:
			case 18219:
				FourSepulchersManager.getInstance().spawnKeyBox(npc);
				break;
			
			case 18150: // Victims.
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				FourSepulchersManager.getInstance().spawnExecutionerOfHalisha(npc);
				cancelQuestTimer("safety", npc, null);
				break;
			
			case 18141:
			case 18142:
			case 18143:
			case 18144:
			case 18145:
			case 18146:
			case 18147:
			case 18148:
				FourSepulchersManager.getInstance().testViscountMobsAnnihilation(npc.getScriptValue());
				break;
			
			case 18220:
			case 18221:
			case 18222:
			case 18223:
			case 18224:
			case 18225:
			case 18226:
			case 18227:
			case 18228:
			case 18229:
			case 18230:
			case 18231: // Petrified statues.
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
				FourSepulchersManager.getInstance().testDukeMobsAnnihilation(npc.getScriptValue());
				break;
			
			case 25339: // Shadows.
			case 25342:
			case 25346:
			case 25349:
				int cupId = 0;
				switch (npc.getNpcId())
				{
					case 25339:
						cupId = 7256;
						break;
					case 25342:
						cupId = 7257;
						break;
					case 25346:
						cupId = 7258;
						break;
					case 25349:
						cupId = 7259;
						break;
				}
				
				final Player player = killer.getActingPlayer();
				if (player != null)
				{
					final Party party = killer.getParty();
					if (party != null)
					{
						for (Player member : party.getMembers())
						{
							final QuestState qs = member.getQuestState(QUEST_ID);
							if (qs != null && (qs.isStarted() || qs.isCompleted()) && member.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null && member.isInsideRadius(npc, Config.PARTY_RANGE, true, false))
								member.addItem("Quest", cupId, 1, member, true);
						}
					}
					else
					{
						final QuestState qs = player.getQuestState(QUEST_ID);
						if (qs != null && (qs.isStarted() || qs.isCompleted()) && player.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null && player.isInsideRadius(npc, Config.PARTY_RANGE, true, false))
							player.addItem("Quest", cupId, 1, player, true);
					}
				}
				FourSepulchersManager.getInstance().spawnEmperorsGraveNpc(npc.getScriptValue());
				break;
		}
		return super.onKill(npc, killer);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		switch (npc.getNpcId())
		{
			case 18150: // Victims.
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				startQuestTimer("safety", 300000, npc, null, false);
				startQuestTimer("aggro", 1000, npc, null, false);
				break;
			
			case 18231: // Petrified statues.
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
			case 18241:
			case 18242:
			case 18243:
				SkillTable.FrequentSkill.FAKE_PETRIFICATION.getSkill().getEffects(npc, npc);
				npc.setIsNoRndWalk(true);
				break;
			
			case 25339: // Shadows.
			case 25342:
			case 25346:
			case 25349:
				((Monster) npc).setRaid(true);
				break;
		}
		return super.onSpawn(npc);
	}
}