package dev.l2j.tesla.gameserver.scripting.scripts.ai.individual;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.manager.GrandBossManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.GrandBoss;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.gameserver.network.serverpackets.SpecialCamera;

/**
 * Dr. Chaos is a boss @ Pavel's Ruins. Some things to know :
 * <ul>
 * <li>As a mad scientist, he thinks all are spies, and for so if you stand too much longer near him you're considered as an "assassin from Black Anvil Guild".</li>
 * <li>You can chat with him, but if you try too much he will become angry.</li>
 * <li>That adaptation sends a decent cinematic made with the different social actions too.</li>
 * <li>The status of the RB is saved under GBs table, in order to retrieve the state if server restarts.</li>
 * <li>The spawn of the different NPCs (Dr. Chaos / War golem) is handled by that script aswell.</li>
 * </ul>
 */
public class DrChaos extends L2AttackableAIScript
{
	private static final int DOCTOR_CHAOS = 32033;
	private static final int CHAOS_GOLEM = 25512;
	
	private static final byte NORMAL = 0; // Dr. Chaos is in NPC form.
	private static final byte CRAZY = 1; // Dr. Chaos entered on golem form.
	private static final byte DEAD = 2; // Dr. Chaos has been killed and has not yet spawned.
	
	private static final String[] SHOUTS =
	{
		"Bwah-ha-ha! Your doom is at hand! Behold the Ultra Secret Super Weapon!",
		"Foolish, insignificant creatures! How dare you challenge me!",
		"I see that none will challenge me now!"
	};
	
	private long _lastAttackTime = 0;
	private int _pissedOffTimer;
	
	public DrChaos()
	{
		super("ai/individual");
		
		addFirstTalkId(DOCTOR_CHAOS); // Different HTMs following actual humor.
		addSpawnId(DOCTOR_CHAOS); // Timer activation at 30sec + paranoia activity.
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(CHAOS_GOLEM);
		int status = GrandBossManager.getInstance().getBossStatus(CHAOS_GOLEM);
		
		// Load the reset date and time for Dr. Chaos from DB.
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
				startQuestTimer("reset_drchaos", temp, null, null, false);
			else
			{
				// The time has already expired while the server was offline. Delete the saved time and
				// immediately spawn Dr. Chaos. Also the state need to be changed for NORMAL
				addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false);
				GrandBossManager.getInstance().setBossStatus(CHAOS_GOLEM, NORMAL);
			}
		}
		// Spawn the war golem.
		else if (status == CRAZY)
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			GrandBoss golem = (GrandBoss) addSpawn(CHAOS_GOLEM, loc_x, loc_y, loc_z, heading, false, 0, false);
			GrandBossManager.getInstance().addBoss(golem);
			
			final Npc _golem = golem;
			
			_golem.setCurrentHpMp(hp, mp);
			_golem.setRunning();
			
			// start monitoring Dr. Chaos's inactivity
			_lastAttackTime = System.currentTimeMillis();
			startQuestTimer("golem_despawn", 60000, _golem, null, true);
		}
		// Spawn the regular NPC.
		else
			addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false);
	}
	
	@Override
	protected void registerNpcs()
	{
		addKillId(CHAOS_GOLEM); // Message + despawn.
		addAttackActId(CHAOS_GOLEM); // Random messages when he attacks.
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("reset_drchaos"))
		{
			GrandBossManager.getInstance().setBossStatus(CHAOS_GOLEM, NORMAL);
			addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false);
		}
		// despawn the live Dr. Chaos after 30 minutes of inactivity
		else if (event.equalsIgnoreCase("golem_despawn") && npc != null)
		{
			if (npc.getNpcId() == CHAOS_GOLEM)
			{
				if (_lastAttackTime + 1800000 < System.currentTimeMillis())
				{
					// Despawn the war golem.
					npc.deleteMe();
					
					addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false); // spawn Dr. Chaos
					GrandBossManager.getInstance().setBossStatus(CHAOS_GOLEM, NORMAL); // mark Dr. Chaos is not crazy any more
					cancelQuestTimer("golem_despawn", npc, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("1"))
		{
			npc.broadcastPacket(new SocialAction(npc, 2));
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -200, 15, 5500, 13500, 0, 0, 1, 0));
		}
		else if (event.equalsIgnoreCase("2"))
			npc.broadcastPacket(new SocialAction(npc, 3));
		else if (event.equalsIgnoreCase("3"))
			npc.broadcastPacket(new SocialAction(npc, 1));
		else if (event.equalsIgnoreCase("4"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3500, 5000, 0, 0, 1, 0));
			npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(95928, -110671, -3340));
		}
		else if (event.equalsIgnoreCase("5"))
		{
			// Delete Dr. Chaos && spawn the war golem.
			npc.deleteMe();
			GrandBoss golem = (GrandBoss) addSpawn(CHAOS_GOLEM, 96080, -110822, -3343, 0, false, 0, false);
			GrandBossManager.getInstance().addBoss(golem);
			
			// The "npc" variable attribution is now for the golem.
			npc = golem;
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 30, 200, 20, 6000, 8000, 0, 0, 1, 0));
			npc.broadcastPacket(new SocialAction(npc, 1));
			npc.broadcastPacket(new PlaySound(1, "Rm03_A", npc));
			
			// start monitoring Dr. Chaos's inactivity
			_lastAttackTime = System.currentTimeMillis();
			startQuestTimer("golem_despawn", 60000, npc, null, true);
		}
		// Check every sec if someone is in range, if found, launch one task to decrease the timer.
		else if (event.equalsIgnoreCase("paranoia_activity"))
		{
			if (GrandBossManager.getInstance().getBossStatus(CHAOS_GOLEM) == NORMAL)
			{
				for (Player obj : npc.getKnownTypeInRadius(Player.class, 500))
				{
					if (obj.isDead())
						continue;
					
					_pissedOffTimer -= 1;
					
					// Make him speak.
					if (_pissedOffTimer == 15)
						npc.broadcastNpcSay("How dare you trespass into my territory! Have you no fear?");
					// That was "too much" for that time.
					else if (_pissedOffTimer <= 0)
						crazyMidgetBecomesAngry(npc);
					
					// Break it here, as we already found a valid player.
					break;
				}
			}
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = "";
		
		if (GrandBossManager.getInstance().getBossStatus(CHAOS_GOLEM) == NORMAL)
		{
			_pissedOffTimer -= Rnd.get(1, 5); // remove 1-5 secs.
			
			if (_pissedOffTimer > 20)
				htmltext = "<html><body>Doctor Chaos:<br>What?! Who are you? How did you come here?<br>You really look suspicious... Aren't those filthy members of Black Anvil guild send you? No? Mhhhhh... I don't trust you!</body></html>";
			else if (_pissedOffTimer > 10 && _pissedOffTimer <= 20)
				htmltext = "<html><body>Doctor Chaos:<br>Why are you standing here? Don't you see it's a private propertie? Don't look at him with those eyes... Did you smile?! Don't make fun of me! He will ... destroy ... you ... if you continue!</body></html>";
			else if (_pissedOffTimer > 0 && _pissedOffTimer <= 10)
				htmltext = "<html><body>Doctor Chaos:<br>I know why you are here, traitor! He discovered your plans! You are assassin ... sent by the Black Anvil guild! But you won't kill the Emperor of Evil!</body></html>";
			else if (_pissedOffTimer <= 0) // That was "too much" for that time.
				crazyMidgetBecomesAngry(npc);
		}
		
		return htmltext;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		// 30 seconds timer at initialization.
		_pissedOffTimer = 30;
		
		// Initialization of the paranoia.
		startQuestTimer("paranoia_activity", 1000, npc, null, true);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		cancelQuestTimer("golem_despawn", npc, null);
		npc.broadcastNpcSay("Urggh! You will pay dearly for this insult.");
		
		// "lock" Dr. Chaos for regular RB time (36H fixed +- 24H random)
		long respawnTime = (36 + Rnd.get(-24, 24)) * 3600000;
		
		GrandBossManager.getInstance().setBossStatus(CHAOS_GOLEM, DEAD);
		startQuestTimer("reset_drchaos", respawnTime, null, null, false);
		
		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(CHAOS_GOLEM);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(CHAOS_GOLEM, info);
		
		return null;
	}
	
	@Override
	public String onAttackAct(Npc npc, Player victim)
	{
		// Choose a message from 3 choices (1/100), and make him speak.
		final int chance = Rnd.get(300);
		if (chance < 3)
			npc.broadcastNpcSay(SHOUTS[chance]);
		
		return null;
	}
	
	/**
	 * Launches the complete animation.
	 * @param npc the midget.
	 */
	private void crazyMidgetBecomesAngry(Npc npc)
	{
		if (GrandBossManager.getInstance().getBossStatus(CHAOS_GOLEM) != NORMAL)
			return;
		
		// Set the status to "crazy".
		GrandBossManager.getInstance().setBossStatus(CHAOS_GOLEM, CRAZY);
		
		// Cancels the paranoia timer.
		cancelQuestTimer("paranoia_activity", npc, null);
		
		// Makes the NPC moves near the Strange Box speaking.
		npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(96323, -110914, -3328));
		npc.broadcastNpcSay("Fools! Why haven't you fled yet? Prepare to learn a lesson!");
		
		// Delayed animation timers.
		startQuestTimer("1", 2000, npc, null, false); // 2 secs, time to launch dr.C anim 2. Cam 1 on.
		startQuestTimer("2", 4000, npc, null, false); // 2,5 secs, time to launch dr.C anim 3.
		startQuestTimer("3", 6500, npc, null, false); // 6 secs, time to launch dr.C anim 1.
		startQuestTimer("4", 12500, npc, null, false); // 4,5 secs to make the NPC moves to the grotto. Cam 2 on.
		startQuestTimer("5", 17000, npc, null, false); // 4 secs for golem spawn, and golem anim. Cam 3 on.
	}
}