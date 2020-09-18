package dev.l2j.tesla.gameserver.scripting.scripts.ai.individual;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData.TeleportType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.Siege;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcSay;

/**
 * Benom is a specific Raid Boss, appearing in Rune Castle. He is aggressive towards anyone.<br>
 * <br>
 * The castle owning clan can defeat Benom. It can teleport to Benom's den using a specific gatekeeper, 24 hours before siege start. If the clan doesn't kill Benom before the siege start, Benom will appear during the siege if at least 2 life controls crystals have been broken.
 */
public class Benom extends L2AttackableAIScript
{
	private static final int BENOM = 29054;
	private static final int TELEPORT_CUBE = 29055;
	private static final int DUNGEON_KEEPER = 35506;
	
	// Important : the heading is used as offset.
	private static final SpawnLocation[] TARGET_TELEPORTS =
	{
		new SpawnLocation(12860, -49158, -976, 650),
		new SpawnLocation(14878, -51339, 1024, 100),
		new SpawnLocation(15674, -49970, 864, 100),
		new SpawnLocation(15696, -48326, 864, 100),
		new SpawnLocation(14873, -46956, 1024, 100),
		new SpawnLocation(12157, -49135, -1088, 650),
		new SpawnLocation(12875, -46392, -288, 200),
		new SpawnLocation(14087, -46706, -288, 200),
		new SpawnLocation(14086, -51593, -288, 200),
		new SpawnLocation(12864, -51898, -288, 200),
		new SpawnLocation(15538, -49153, -1056, 200),
		new SpawnLocation(17001, -49149, -1064, 650)
	};
	
	private static final SpawnLocation THRONE_LOC = new SpawnLocation(11025, -49152, -537, 0);
	private static final SpawnLocation PRISON_LOC = new SpawnLocation(11882, -49216, -3008, 0);
	
	private Siege _siege;
	
	private Npc _benom;
	
	private boolean _isPrisonOpened;
	
	private List<Player> _targets = new ArrayList<>();
	
	public Benom()
	{
		super("ai/individual");
		
		_siege = addSiegeNotify(8);
		
		addStartNpc(DUNGEON_KEEPER, TELEPORT_CUBE);
		addTalkId(DUNGEON_KEEPER, TELEPORT_CUBE);
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(BENOM, ScriptEventType.ON_AGGRO, ScriptEventType.ON_SPELL_FINISHED, ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		switch (npc.getNpcId())
		{
			case TELEPORT_CUBE:
				talker.teleportTo(TeleportType.TOWN);
				break;
			
			case DUNGEON_KEEPER:
				if (_isPrisonOpened)
					talker.teleportTo(12589, -49044, -3008, 0);
				else
					return HtmCache.getInstance().getHtm("data/html/doormen/35506-2.htm");
				break;
		}
		return super.onTalk(npc, talker);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "benom_spawn":
				_isPrisonOpened = true;
				
				_benom = addSpawn(BENOM, PRISON_LOC, false, 0, false);
				_benom.broadcastNpcSay("Who dares to covet the throne of our castle! Leave immediately or you will pay the price of your audacity with your very own blood!");
				break;
			
			case "tower_check":
				if (_siege.getControlTowerCount() < 2)
				{
					npc.teleportTo(THRONE_LOC, 0);
					_siege.getCastle().getSiegeZone().broadcastPacket(new NpcSay(0, Say2.ALL, DUNGEON_KEEPER, "Oh no! The defenses have failed. It is too dangerous to remain inside the castle. Flee! Every man for himself!"));
					
					cancelQuestTimer("tower_check", npc, null);
					startQuestTimer("raid_check", 10000, npc, null, true);
				}
				break;
			
			case "raid_check":
				if (!npc.isInsideZone(ZoneId.SIEGE) && !npc.isTeleporting())
					npc.teleportTo(THRONE_LOC, 0);
				break;
		}
		return event;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (isPet)
			return super.onAggro(npc, player, isPet);
		
		if (_targets.size() < 10 && Rnd.get(3) < 1)
			_targets.add(player);
		
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public void onSiegeEvent()
	{
		// Don't go further if the castle isn't owned.
		if (_siege.getCastle().getOwnerId() <= 0)
			return;
		
		switch (_siege.getStatus())
		{
			case IN_PROGRESS:
				_isPrisonOpened = false;
				if (_benom != null && !_benom.isDead())
					startQuestTimer("tower_check", 30000, _benom, null, true);
				break;
			
			case REGISTRATION_OPENED:
				_isPrisonOpened = false;
				
				if (_benom != null)
				{
					cancelQuestTimer("tower_check", _benom, null);
					cancelQuestTimer("raid_check", _benom, null);
					
					_benom.deleteMe();
				}
				
				startQuestTimer("benom_spawn", _siege.getSiegeDate().getTimeInMillis() - 8640000 - System.currentTimeMillis(), null, null, false);
				break;
			
			case REGISTRATION_OVER:
				startQuestTimer("benom_spawn", 0, null, null, false);
				break;
		}
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, L2Skill skill)
	{
		switch (skill.getId())
		{
			case 4995:
				teleportTarget(player);
				((Attackable) npc).stopHating(player);
				break;
			
			case 4996:
				teleportTarget(player);
				((Attackable) npc).stopHating(player);
				if (!_targets.isEmpty())
				{
					for (Player target : _targets)
					{
						final long x = player.getX() - target.getX();
						final long y = player.getY() - target.getY();
						final long z = player.getZ() - target.getZ();
						final long range = 250;
						if (((x * x) + (y * y) + (z * z)) <= (range * range))
						{
							teleportTarget(target);
							((Attackable) npc).stopHating(target);
						}
					}
					_targets.clear();
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			if (Rnd.get(100) <= 25)
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4995, 1));
			}
			else if (!npc.isCastingNow())
			{
				if ((npc.getCurrentHp() < (npc.getMaxHp() / 3)) && Rnd.get(500) < 1)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4996, 1));
				}
				else if (!npc.isInsideRadius(attacker, 300, true, false) && Rnd.get(100) < 1)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4993, 1));
				}
				else if (Rnd.get(100) < 1)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4994, 1));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		npc.broadcastNpcSay("It's not over yet... It won't be... over... like this... Never...");
		cancelQuestTimer("raid_check", npc, null);
		
		addSpawn(TELEPORT_CUBE, 12589, -49044, -3008, 0, false, 120000, false);
		
		return null;
	}
	
	/**
	 * Move a player by Skill. Venom has two skill related.
	 * @param player the player targeted
	 */
	private static void teleportTarget(Player player)
	{
		if (player != null)
		{
			final SpawnLocation loc = Rnd.get(TARGET_TELEPORTS);
			player.teleportTo(loc, loc.getHeading());
		}
	}
}