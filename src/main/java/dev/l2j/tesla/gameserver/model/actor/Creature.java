package dev.l2j.tesla.gameserver.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.handler.ISkillHandler;
import dev.l2j.tesla.gameserver.handler.SkillHandler;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.holder.SkillUseHolder;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.*;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.skills.Calculator;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.skills.effects.EffectChanceSkillTrigger;
import dev.l2j.tesla.gameserver.taskmanager.AttackStanceTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.MovementTaskManager;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable.FrequentSkill;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData.TeleportType;
import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.GaugeColor;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.enums.items.WeaponType;
import dev.l2j.tesla.gameserver.enums.skills.AbnormalEffect;
import dev.l2j.tesla.gameserver.enums.skills.FlyType;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectFlag;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.ChanceSkillList;
import dev.l2j.tesla.gameserver.model.CharEffectList;
import dev.l2j.tesla.gameserver.model.FusionSkill;
import dev.l2j.tesla.gameserver.model.IChanceSkillTrigger;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.WorldRegion;
import dev.l2j.tesla.gameserver.model.actor.ai.type.AttackableAI;
import dev.l2j.tesla.gameserver.model.actor.ai.type.CreatureAI;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.RiftInvader;
import dev.l2j.tesla.gameserver.model.actor.instance.Walker;
import dev.l2j.tesla.gameserver.model.actor.stat.CreatureStat;
import dev.l2j.tesla.gameserver.model.actor.status.CreatureStatus;
import dev.l2j.tesla.gameserver.model.actor.template.CreatureTemplate;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Armor;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.Attack;
import dev.l2j.tesla.gameserver.network.serverpackets.ChangeMoveType;
import dev.l2j.tesla.gameserver.network.serverpackets.ChangeWaitType;
import dev.l2j.tesla.gameserver.network.serverpackets.FlyToLocation;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillCanceled;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillLaunched;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToLocation;
import dev.l2j.tesla.gameserver.network.serverpackets.Revive;
import dev.l2j.tesla.gameserver.network.serverpackets.ServerObjectInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.SetupGauge;
import dev.l2j.tesla.gameserver.network.serverpackets.StatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.StopMove;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.TeleportToLocation;
import dev.l2j.tesla.gameserver.skills.funcs.FuncAtkAccuracy;
import dev.l2j.tesla.gameserver.skills.funcs.FuncAtkCritical;
import dev.l2j.tesla.gameserver.skills.funcs.FuncAtkEvasion;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMAtkCritical;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMAtkMod;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMAtkSpeed;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMDefMod;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMaxHpMul;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMaxMpMul;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMoveSpeed;
import dev.l2j.tesla.gameserver.skills.funcs.FuncPAtkMod;
import dev.l2j.tesla.gameserver.skills.funcs.FuncPAtkSpeed;
import dev.l2j.tesla.gameserver.skills.funcs.FuncPDefMod;

/**
 * An instance type extending {@link WorldObject} which represents the mother class of all character objects of the world such as players, NPCs and monsters.
 */
public abstract class Creature extends WorldObject
{
	private volatile boolean _isCastingNow = false;
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false;
	private boolean _isParalyzed = false;
	private boolean _isDead = false;
	private boolean _isRunning = false;
	protected boolean _isTeleporting = false;
	protected boolean _showSummonAnimation = false;
	
	protected boolean _isInvul = false;
	private boolean _isMortal = true;
	
	private boolean _isNoRndWalk = false;
	private boolean _AIdisabled = false;
	
	private CreatureStat _stat;
	private CreatureStatus _status;
	private CreatureTemplate _template; // The link on the L2CharTemplate object containing generic and static properties
	
	protected String _title;
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _champion = false;
	
	private final Calculator[] _calculators;
	
	private ChanceSkillList _chanceSkills;
	protected FusionSkill _fusionSkill;
	
	private final byte[] _zones = new byte[ZoneId.VALUES.length];
	protected byte _zoneValidateCounter = 4;
	
	public Creature(int objectId, CreatureTemplate template)
	{
		super(objectId);
		initCharStat();
		initCharStatus();
		
		// Set its template to the new Creature
		_template = template;
		
		_calculators = new Calculator[Stats.NUM_STATS];
		addFuncsToNewCharacter();
	}
	
	/**
	 * This method is overidden in
	 * <ul>
	 * <li>Player</li>
	 * <li>L2DoorInstance</li>
	 * </ul>
	 */
	public void addFuncsToNewCharacter()
	{
		addStatFunc(FuncPAtkMod.getInstance());
		addStatFunc(FuncMAtkMod.getInstance());
		addStatFunc(FuncPDefMod.getInstance());
		addStatFunc(FuncMDefMod.getInstance());
		
		addStatFunc(FuncMaxHpMul.getInstance());
		addStatFunc(FuncMaxMpMul.getInstance());
		
		addStatFunc(FuncAtkAccuracy.getInstance());
		addStatFunc(FuncAtkEvasion.getInstance());
		
		addStatFunc(FuncPAtkSpeed.getInstance());
		addStatFunc(FuncMAtkSpeed.getInstance());
		
		addStatFunc(FuncMoveSpeed.getInstance());
		
		addStatFunc(FuncAtkCritical.getInstance());
		addStatFunc(FuncMAtkCritical.getInstance());
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}
	
	/**
	 * Remove the Creature from the world when the decay task is launched.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _objects of World.</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players.</B></FONT>
	 */
	public void onDecay()
	{
		decayMe();
	}
	
	public void onTeleported()
	{
		if (!isTeleporting())
			return;
		
		setIsTeleporting(false);
		
		setRegion(World.getInstance().getRegion(getPosition()));
	}
	
	public Inventory getInventory()
	{
		return null;
	}
	
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		return true;
	}
	
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		return true;
	}
	
	@Override
	public boolean isInsideZone(ZoneId zone)
	{
		return zone == ZoneId.PVP ? _zones[ZoneId.PVP.getId()] > 0 && _zones[ZoneId.PEACE.getId()] == 0 : _zones[zone.getId()] > 0;
	}
	
	public void setInsideZone(ZoneId zone, boolean state)
	{
		if (state)
			_zones[zone.getId()]++;
		else
		{
			_zones[zone.getId()]--;
			if (_zones[zone.getId()] < 0)
				_zones[zone.getId()] = 0;
		}
	}
	
	/**
	 * @return true if the player is GM.
	 */
	public boolean isGM()
	{
		return false;
	}
	
	/**
	 * Send a {@link L2GameServerPacket} to all known {@link Player}s.
	 * @param packet : The packet to send.
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		broadcastPacket(packet, true);
	}
	
	/**
	 * Send a {@link L2GameServerPacket} to all known {@link Player}s. Overidden on Player, which uses selfToo boolean flag to send the packet to self.
	 * @param packet : The packet to send.
	 * @param selfToo : If true, we also send it to self.
	 */
	public void broadcastPacket(L2GameServerPacket packet, boolean selfToo)
	{
		for (Player player : getKnownType(Player.class))
			player.sendPacket(packet);
	}
	
	/**
	 * Send a {@link L2GameServerPacket} to self and to all known {@link Player}s in a given radius. Overidden on Player, which also send the packet to self.
	 * @param packet : The packet to send.
	 * @param radius : The radius to check.
	 */
	public void broadcastPacketInRadius(L2GameServerPacket packet, int radius)
	{
		if (radius < 0)
			radius = 600;
		
		for (Player player : getKnownTypeInRadius(Player.class, radius))
			player.sendPacket(packet);
	}
	
	/**
	 * @param barPixels
	 * @return boolean true if hp update should be done, false if not.
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		
		if (currentHp <= 1.0 || getMaxHp() < barPixels)
			return true;
		
		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all Creature called _statusListener that must be informed of HP/MP updates of this Creature</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR>
	 * <BR>
	 * <B><U>Overriden in Player</U></B> : Send current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
			return;
		
		if (!needHpUpdate(352))
			return;
		
		// Create the Server->Client packet StatusUpdate with current HP
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		
		// Go through the StatusListener
		for (Creature temp : getStatus().getStatusListener())
		{
			if (temp != null)
				temp.sendPacket(su);
		}
	}
	
	/**
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>Player</li><BR>
	 * <BR>
	 * @param mov The packet to send.
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}
	
	/**
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li>Player</li><BR>
	 * <BR>
	 * @param text The string to send.
	 */
	public void sendMessage(String text)
	{
		// default implementation
	}
	
	/**
	 * Instantly teleport this {@link Creature} to defined coordinates X/Y/Z.<br>
	 * <br>
	 * <b>BEWARE : has to be used on really short distances (mostly only skills), since there isn't any region edit.</b>.
	 * @param x : The X coord to set.
	 * @param y : The Y coord to set.
	 * @param z : The Z coord to set.
	 * @param randomOffset : If > 0, we randomize the teleport location.
	 */
	public void instantTeleportTo(int x, int y, int z, int randomOffset)
	{
		stopMove(null);
		abortAttack();
		abortCast();
		
		setTarget(null);
		
		getAI().setIntention(IntentionType.ACTIVE);
		
		if (randomOffset > 0)
		{
			x += Rnd.get(-randomOffset, randomOffset);
			y += Rnd.get(-randomOffset, randomOffset);
		}
		
		z += 5;
		
		// Broadcast TeleportToLocation packet.
		broadcastPacket(new TeleportToLocation(this, x, y, z, true));
		
		// Set the position.
		getPosition().set(x, y, z);
		
		// Refresh knownlist.
		refreshKnownlist();
	}
	
	/**
	 * Instantly teleport this {@link Creature} to a defined {@link Location}.<br>
	 * <br>
	 * <b>BEWARE : has to be used on really short distances (mostly only skills), since there isn't any region edit.</b>.
	 * @param loc : The Location to teleport to.
	 * @param randomOffset : If > 0, we randomize the teleport location.
	 */
	public void instantTeleportTo(Location loc, int randomOffset)
	{
		instantTeleportTo(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
	}
	
	/**
	 * Teleport this {@link Creature} to defined coordinates X/Y/Z.
	 * @param x : The X coord to set.
	 * @param y : The Y coord to set.
	 * @param z : The Z coord to set.
	 * @param randomOffset : If > 0, we randomize the teleport location.
	 */
	public void teleportTo(int x, int y, int z, int randomOffset)
	{
		stopMove(null);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		getAI().setIntention(IntentionType.ACTIVE);
		
		if (randomOffset > 0)
		{
			x += Rnd.get(-randomOffset, randomOffset);
			y += Rnd.get(-randomOffset, randomOffset);
		}
		
		z += 5;
		
		// Broadcast TeleportToLocation packet.
		broadcastPacket(new TeleportToLocation(this, x, y, z, false));
		
		// Remove the object from its old location.
		setRegion(null);
		
		// Set the position.
		getPosition().set(x, y, z);
		
		// Handle onTeleported behavior, but only if it's not a Player. Players are handled from Appearing packet.
		if (!(this instanceof Player) || (((Player) this).getClient() != null && ((Player) this).getClient().isDetached()))
			onTeleported();
	}
	
	/**
	 * Teleport this {@link Creature} to a defined {@link Location}.
	 * @param loc : The Location to teleport to.
	 * @param randomOffset : If > 0, we randomize the teleport location.
	 */
	public void teleportTo(Location loc, int randomOffset)
	{
		teleportTo(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
	}
	
	/**
	 * Teleport this {@link Creature} to a defined {@link TeleportType} (CASTLE, CLAN_HALL, SIEGE_FLAG, TOWN).
	 * @param type : The TeleportType to teleport to.
	 */
	public void teleportTo(TeleportType type)
	{
		teleportTo(MapRegionData.getInstance().getLocationToTeleport(this, type), 20);
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get the active weapon (always equipped in the right hand)</li>
	 * </ul>
	 * <ul>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the Player with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li>
	 * </ul>
	 * <ul>
	 * <li>Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the Creature AND to all Player in the _KnownPlayers of the Creature</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li>
	 * </ul>
	 * @param target The Creature targeted
	 */
	public void doAttack(Creature target)
	{
		if (target == null || isAttackingDisabled())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!isAlikeDead())
		{
			if (this instanceof Npc && target.isAlikeDead() || !getKnownType(Creature.class).contains(target))
			{
				getAI().setIntention(IntentionType.ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (this instanceof Player && target.isDead())
			{
				getAI().setIntention(IntentionType.ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		final Player player = getActingPlayer();
		
		if (player != null && player.isInObserverMode())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Checking if target has moved to peace zone
		if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(IntentionType.ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		stopEffectsOnAction();
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		final Weapon weaponItem = getActiveWeaponItem();
		final WeaponType weaponItemType = getAttackType();
		
		if (weaponItemType == WeaponType.FISHINGROD)
		{
			// You can't make an attack with a fishing pole.
			getAI().setIntention(IntentionType.IDLE);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoEngine.getInstance().canSeeTarget(this, target))
		{
			getAI().setIntention(IntentionType.ACTIVE);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final long time = System.currentTimeMillis();
		
		// Bow checks.
		if (weaponItemType == WeaponType.BOW)
		{
			if (this instanceof Player)
			{
				// Equip needed arrows in left hand ; if it's not possible, cancel the action.
				if (!checkAndEquipArrows())
				{
					getAI().setIntention(IntentionType.IDLE);
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the bow can be used. Cancel the action if the bow can't be re-use at this moment.
				if (_disableBowAttackEndTime > time)
				{
					ThreadPool.schedule(() -> getAI().notifyEvent(AiEventType.READY_TO_ACT), 100);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the Player owns enough MP. If not, stop the attack.
				final int mpConsume = weaponItem.getMpConsume();
				if (mpConsume > 0)
				{
					if (getCurrentMp() < mpConsume)
					{
						getAI().setIntention(IntentionType.IDLE);
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					getStatus().reduceMp(mpConsume);
				}
			}
			else if (this instanceof Npc)
			{
				if (_disableBowAttackEndTime > time)
					return;
			}
		}
		
		// Recharge any active auto soulshot tasks for current Creature instance.
		rechargeShots(true, false);
		
		// Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItemType);
		_attackEndTime = time + timeAtk - 100;
		_disableBowAttackEndTime = time + 50;
		
		// Create Attack
		Attack attack = new Attack(this, isChargedShot(ShotType.SOULSHOT), (weaponItem != null) ? weaponItem.getCrystalType().getId() : 0);
		
		// Make sure that char is facing selected target
		getPosition().setHeading(MathUtil.calculateHeadingFrom(this, target));
		
		boolean hitted;
		
		// Select the type of attack to start
		switch (weaponItemType)
		{
			case BOW:
				hitted = doAttackHitByBow(attack, target, timeAtk, weaponItem);
				break;
			
			case POLE:
				hitted = doAttackHitByPole(attack, target, timeAtk / 2);
				break;
			
			case DUAL:
			case DUALFIST:
				hitted = doAttackHitByDual(attack, target, timeAtk / 2);
				break;
			
			case FIST:
				if (getSecondaryWeaponItem() != null && getSecondaryWeaponItem() instanceof Armor)
					hitted = doAttackHitSimple(attack, target, timeAtk / 2);
				else
					hitted = doAttackHitByDual(attack, target, timeAtk / 2);
				break;
			
			default:
				hitted = doAttackHitSimple(attack, target, timeAtk / 2);
				break;
		}
		
		// Refresh the attack stance.
		getAI().startAttackStance();
		
		// Refresh PvP status of the attacker.
		if (player != null && player.getSummon() != target)
			player.updatePvPStatus(target);
		
		// Check if hit isn't missed
		if (!hitted)
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			abortAttack();
		else
		{
			// IA implementation for ON_ATTACK_ACT (mob which attacks a player).
			if (this instanceof Attackable)
			{
				// Bypass behavior if the victim isn't a player
				final Player victim = target.getActingPlayer();
				if (victim != null)
				{
					final Npc mob = ((Npc) this);
					
					final List<Quest> scripts = mob.getTemplate().getEventQuests(ScriptEventType.ON_ATTACK_ACT);
					if (scripts != null)
						for (Quest quest : scripts)
							quest.notifyAttackAct(mob, victim);
				}
			}
			
			// If we didn't miss the hit, discharge the shoulshots, if any
			setChargedShot(ShotType.SOULSHOT, false);
			
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
						target.setCurrentCp(0);
				}
				else if (player.isHero())
				{
					if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
						// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
						target.setCurrentCp(0);
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the Creature AND to all Player in the _KnownPlayers of the Creature
		if (attack.hasHits())
			broadcastPacket(attack);
		
		// If target isn't attackable anymore, player's auto attacks should be stopped.
		if (player != null && !target.isAutoAttackable(player))
		{
			getAI().setIntention(IntentionType.IDLE);
			return;
		}
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPool.schedule(() -> getAI().notifyEvent(AiEventType.READY_TO_ACT), timeAtk);
	}
	
	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consumme arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the Creature is a Player, Send SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param weapon The weapon, which is attacker using
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, Creature target, int sAtk, Weapon weapon)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume arrows
		reduceArrowCount();
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target, null);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
		}
		
		// Get the Attack Reuse Delay of the Weapon
		int reuse = weapon.getReuseDelay();
		if (reuse != 0)
			reuse = (reuse * 345) / getStat().getPAtkSpd();
		
		// Check if the Creature is a Player
		if (this instanceof Player)
		{
			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send SetupGauge
			sendPacket(new SetupGauge(GaugeColor.RED, sAtk + reuse));
		}
		
		// Create a new hit task with Medium priority
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime += (sAtk + reuse);
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target, null);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target, null);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, attack.soulshot);
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPool.schedule(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get all visible objects in a spherical area near the Creature to obtain possible targets</li>
	 * <li>If possible target is the Creature targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the Creature targeted but is attackable, launch a simple attack against it</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, Creature target, int sAtk)
	{
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		// Get the number of targets (-1 because the main target is already used)
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 0, null, null) - 1;
		int attackcount = 0;
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		
		for (Creature obj : getKnownType(Creature.class))
		{
			if (obj == target || obj.isAlikeDead())
				continue;
			
			if (this instanceof Player)
			{
				if (obj instanceof Pet && ((Pet) obj).getOwner() == ((Player) this))
					continue;
			}
			else if (this instanceof Attackable)
			{
				if (obj instanceof Player && getTarget() instanceof Attackable)
					continue;
				
				if (obj instanceof Attackable && !isConfused())
					continue;
			}
			
			if (!MathUtil.checkIfInRange(maxRadius, this, obj, false))
				continue;
			
			// otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
			if (Math.abs(obj.getZ() - getZ()) > 650)
				continue;
			
			if (!isFacing(obj, maxAngleDiff))
				continue;
			
			// Launch an attack on each character, until attackRandomCountMax is reached.
			if (obj == getAI().getTarget() || obj.isAutoAttackable(this))
			{
				attackcount++;
				if (attackcount > attackRandomCountMax)
					break;
				
				hitted |= doAttackHitSimple(attack, obj, attackpercent, sAtk);
				attackpercent /= 1.15;
			}
		}
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, Creature target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, Creature target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target, null);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.soulshot);
			
			if (attackpercent != 100)
				damage1 = (int) (damage1 * attackpercent / 100);
		}
		
		// Create a new hit task with Medium priority
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}
	
	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			
			if (this instanceof Player)
				getAI().setIntention(IntentionType.ACTIVE);
			
			return;
		}
		// Override casting type
		if (skill.isSimultaneousCast() && !simultaneously)
			simultaneously = true;
		
		stopEffectsOnAction();
		
		// Recharge AutoSoulShot
		rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
		
		// Set the target of the skill in function of Skill Type and Target Type
		Creature target = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		WorldObject[] targets = skill.getTargetList(this);
		
		boolean doit = false;
		
		// AURA skills should always be using caster as target
		switch (skill.getTargetType())
		{
			case TARGET_AREA_SUMMON: // We need it to correct facing
				target = getSummon();
				break;
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
				target = this;
				break;
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_OWNER_PET:
			case TARGET_PARTY:
			case TARGET_CLAN:
			case TARGET_ALLY:
				doit = true;
			default:
				if (targets.length == 0)
				{
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					// Send ActionFailed to the Player
					if (this instanceof Player)
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(IntentionType.ACTIVE);
					}
					return;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
					case HEAL:
					case COMBATPOINTHEAL:
					case MANAHEAL:
					case SEED:
					case REFLECT:
						doit = true;
						break;
				}
				
				target = (doit) ? (Creature) targets[0] : (Creature) getTarget();
		}
		beginCast(skill, simultaneously, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously, Creature target, WorldObject[] targets)
	{
		if (target == null)
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			
			if (this instanceof Player)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(IntentionType.ACTIVE);
			}
			return;
		}
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		
		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FUSION skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting)
		{
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
		}
		
		// Calculate altered Cast Speed due to BSpS/SpS
		if (skill.isMagic() && !effectWhileCasting)
		{
			// Only takes 70% of the time to cast a BSpS/SpS cast
			if (isChargedShot(ShotType.SPIRITSHOT) || isChargedShot(ShotType.BLESSED_SPIRITSHOT))
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
			}
		}
		
		// Don't modify skills HitTime if staticHitTime is specified for skill in datapack.
		if (skill.isStaticHitTime())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		// if basic hitTime is higher than 500 than the min hitTime is 500
		else if (skill.getHitTime() >= 500 && hitTime < 500)
			hitTime = 500;
		
		// Set the _castInterruptTime and casting status (Player already has this true)
		if (simultaneously)
		{
			// queue herbs and potions
			if (isCastingSimultaneouslyNow())
			{
				ThreadPool.schedule(() -> doSimultaneousCast(skill), 100);
				return;
			}
			setIsCastingSimultaneouslyNow(true);
			setLastSimultaneousSkillCast(skill);
		}
		else
		{
			setIsCastingNow(true);
			_castInterruptTime = System.currentTimeMillis() + hitTime - 200;
			setLastSkillCast(skill);
		}
		
		// Init the reuse time of the skill
		int reuseDelay = skill.getReuseDelay();
		
		if (!skill.isStaticReuse())
		{
			reuseDelay *= calcStat(skill.isMagic() ? Stats.MAGIC_REUSE_RATE : Stats.P_REUSE, 1, null, null);
			reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		}
		
		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		
		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery)
			addTimeStamp(skill, reuseDelay);
		
		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
				
				if (getActingPlayer() != null)
					getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
			}
			
			disableSkill(skill, reuseDelay);
		}
		
		// Make sure that char is facing selected target
		if (target != this)
			getPosition().setHeading(MathUtil.calculateHeadingFrom(this, target));
		
		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the Creature
			if (skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					
					if (this instanceof Player)
						getAI().setIntention(IntentionType.ACTIVE);
					return;
				}
			}
			
			if (skill.getSkillType() == L2SkillType.FUSION)
				startFusionSkill(target, skill);
			else
				callSkill(skill, targets);
		}
		
		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		if (level < 1)
			level = 1;
		
		// Broadcast MagicSkillUse for non toggle skills.
		if (!skill.isToggle())
		{
			if (!skill.isPotion())
			{
				broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, false));
				broadcastPacket(new MagicSkillLaunched(this, displayId, level, (targets == null || targets.length == 0) ? new WorldObject[]
				{
					target
				} : targets));
			}
			else
				broadcastPacket(new MagicSkillUse(this, target, displayId, level, 0, 0));
		}
		
		if (this instanceof Playable)
		{
			// Send a system message USE_S1 to the Creature
			if (this instanceof Player && skill.getId() != 1312)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
				sm.addSkillName(skill);
				sendPacket(sm);
			}
			
			if (!effectWhileCasting && skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}
			}
			
			// Before start AI Cast Broadcast Fly Effect is Need
			if (this instanceof Player && skill.getFlyType() != null)
				ThreadPool.schedule(new FlyToLocationTask(this, target, skill), 50);
		}
		
		MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously);
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 410)
		{
			// Send SetupGauge with the color of the gauge and the casting time
			if (this instanceof Player && !effectWhileCasting)
				sendPacket(new SetupGauge(GaugeColor.BLUE, hitTime));
			
			if (effectWhileCasting)
				mut.phase = 2;
			
			if (simultaneously)
			{
				Future<?> future = _skillCast2;
				if (future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast2 = ThreadPool.schedule(mut, hitTime - 400);
			}
			else
			{
				Future<?> future = _skillCast;
				if (future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast = ThreadPool.schedule(mut, hitTime - 400);
			}
		}
		else
		{
			mut.hitTime = 0;
			onMagicLaunchedTimer(mut);
		}
	}
	
	/**
	 * Check if casting of skill is possible
	 * @param skill
	 * @return True if casting is possible
	 */
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (skill == null || isSkillDisabled(skill))
		{
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Verify the different types of silence (magic and physic)
		if (!skill.isPotion() && ((skill.isMagic() && isMuted()) || (!skill.isMagic() && isPhysicalMuted())))
		{
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the spell consumes an Item
		if (skill.getItemConsumeId() > 0 && getInventory() != null)
		{
			// Get the ItemInstance consumed by the spell
			ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
					return false;
				}
				
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NUMBER_INCORRECT));
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Index according to skill id the current timestamp of use, overridden in Player.
	 * @param skill id
	 * @param reuse delay
	 */
	public void addTimeStamp(L2Skill skill, long reuse)
	{
	}
	
	public void startFusionSkill(Creature target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
			return;
		
		if (_fusionSkill == null)
			_fusionSkill = new FusionSkill(this, target, skill);
	}
	
	/**
	 * Kill this {@link Creature}.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the Creature</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform</li>
	 * <li>Notify Creature AI</li>
	 * </ul>
	 * <B><U> Overridden in </U> :</B>
	 * <ul>
	 * <li>Npc : Create a DecayTask to remove the corpse of the Npc after 7 seconds</li>
	 * <li>Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine</li>
	 * <li>Player : Apply Death Penalty, Manage gain/loss Karma and Item Drop</li>
	 * </ul>
	 * @param killer : The Creature who killed it
	 * @return true if successful.
	 */
	public boolean doDie(Creature killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
				return false;
			
			// now reset currentHp to zero
			setCurrentHp(0);
			
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop Regeneration task, and removes all current effects
		getStatus().stopHpMpRegeneration();
		stopAllEffectsExceptThoseThatLastThroughDeath();
		
		calculateRewards(killer);
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate();
		
		// Notify Creature AI
		if (hasAI())
			getAI().notifyEvent(AiEventType.DEAD, null);
		
		return true;
	}
	
	public void deleteMe()
	{
		if (hasAI())
			getAI().stopAITask();
	}
	
	public void detachAI()
	{
		_ai = null;
	}
	
	protected void calculateRewards(Creature killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the Creature. */
	public void doRevive()
	{
		if (!isDead() || isTeleporting())
			return;
		
		setIsDead(false);
		
		_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
		
		// Start broadcast status
		broadcastPacket(new Revive(this));
	}
	
	/**
	 * Revives the Creature using skill.
	 * @param revivePower
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	/**
	 * @return the CreatureAI of the Creature and if its null create a new one.
	 */
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new CreatureAI(this);
				
				return _ai;
			}
		}
		return ai;
	}
	
	public void setAI(CreatureAI newAI)
	{
		CreatureAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof AttackableAI)
			((AttackableAI) oldAI).stopAITask();
		
		_ai = newAI;
	}
	
	/**
	 * @return true if this object has a running AI.
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * @return true if this object is a raid boss.
	 */
	public boolean isRaidBoss()
	{
		return false;
	}
	
	/**
	 * @return true if this object is either a raid minion or a raid boss.
	 */
	public boolean isRaidRelated()
	{
		return false;
	}
	
	/**
	 * @return true if this object is a minion.
	 */
	public boolean isMinion()
	{
		return false;
	}
	
	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}
	
	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}
	
	public final void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}
	
	public final boolean isAfraid()
	{
		return isAffected(L2EffectFlag.FEAR);
	}
	
	public final boolean isConfused()
	{
		return isAffected(L2EffectFlag.CONFUSED);
	}
	
	public final boolean isMuted()
	{
		return isAffected(L2EffectFlag.MUTED);
	}
	
	public final boolean isPhysicalMuted()
	{
		return isAffected(L2EffectFlag.PHYSICAL_MUTED);
	}
	
	public final boolean isRooted()
	{
		return isAffected(L2EffectFlag.ROOTED);
	}
	
	public final boolean isSleeping()
	{
		return isAffected(L2EffectFlag.SLEEP);
	}
	
	public final boolean isStunned()
	{
		return isAffected(L2EffectFlag.STUNNED);
	}
	
	public final boolean isBetrayed()
	{
		return isAffected(L2EffectFlag.BETRAYED);
	}
	
	public final boolean isImmobileUntilAttacked()
	{
		return isAffected(L2EffectFlag.MEDITATING);
	}
	
	/**
	 * @return True if the Creature can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isImmobileUntilAttacked() || isSleeping() || isParalyzed();
	}
	
	/**
	 * BEWARE : don't use isAttackingNow() instead of _attackEndTime > System.currentTimeMillis(), as it's overidden on L2Summon.
	 * @return True if the Creature can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse).
	 */
	public boolean isAttackingDisabled()
	{
		return isFlying() || isStunned() || isImmobileUntilAttacked() || isSleeping() || _attackEndTime > System.currentTimeMillis() || isParalyzed() || isAlikeDead() || isCoreAIDisabled();
	}
	
	public final Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public boolean isImmobilized()
	{
		return _isImmobilized;
	}
	
	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}
	
	/**
	 * @return True if the Creature is dead or use fake death.
	 */
	public boolean isAlikeDead()
	{
		return _isDead;
	}
	
	/**
	 * @return True if the Creature is dead.
	 */
	public final boolean isDead()
	{
		return _isDead;
	}
	
	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}
	
	/**
	 * @return True if the Creature is in a state where he can't move.
	 */
	public boolean isMovementDisabled()
	{
		return isStunned() || isImmobileUntilAttacked() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isAlikeDead() || isTeleporting();
	}
	
	/**
	 * @return True if the Creature is in a state where he can't be controlled.
	 */
	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isParalyzed() || isStunned() || isSleeping();
	}
	
	/**
	 * @return true if the {@link Creature} is in a state where he can't attack.
	 */
	public boolean cantAttack()
	{
		return isStunned() || isImmobileUntilAttacked() || isAfraid() || isSleeping() || isParalyzed() || isAlikeDead() || isTeleporting();
	}
	
	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _isParalyzed || isAffected(L2EffectFlag.PARALYZED);
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}
	
	/**
	 * Overriden in {@link Player}.
	 * @return the {@link Summon} of this {@link Creature}.
	 */
	public Summon getSummon()
	{
		return null;
	}
	
	public boolean isSeated()
	{
		return false;
	}
	
	public boolean isRiding()
	{
		return false;
	}
	
	public boolean isFlying()
	{
		return false;
	}
	
	public final boolean isRunning()
	{
		return _isRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getMoveSpeed() != 0)
			broadcastPacket(new ChangeMoveType(this));
		
		if (this instanceof Player)
			((Player) this).broadcastUserInfo();
		else if (this instanceof Summon)
			((Summon) this).broadcastStatusUpdate();
		else if (this instanceof Npc)
		{
			for (Player player : getKnownType(Player.class))
			{
				if (getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((Npc) this, player));
				else
					player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) this, player));
			}
		}
	}
	
	/** Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player. */
	public final void setRunning()
	{
		if (!isRunning())
			setIsRunning(true);
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}
	
	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}
	
	public boolean isMortal()
	{
		return _isMortal;
	}
	
	/**
	 * @return true if this {@link Creature} is undead. Overidden in {@link Npc}.
	 */
	public boolean isUndead()
	{
		return false;
	}
	
	public void initCharStat()
	{
		_stat = new CreatureStat(this);
	}
	
	public CreatureStat getStat()
	{
		return _stat;
	}
	
	public final void setStat(CreatureStat value)
	{
		_stat = value;
	}
	
	public void initCharStatus()
	{
		_status = new CreatureStatus(this);
	}
	
	public CreatureStatus getStatus()
	{
		return _status;
	}
	
	public final void setStatus(CreatureStatus value)
	{
		_status = value;
	}
	
	public CreatureTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * Set the template of the Creature.<BR>
	 * <BR>
	 * Each Creature owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of Creature. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of Creature
	 * is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B>
	 * @param template The template to set up.
	 */
	protected final void setTemplate(CreatureTemplate template)
	{
		_template = template;
	}
	
	/**
	 * @return the Title of the Creature.
	 */
	public final String getTitle()
	{
		return _title;
	}
	
	/**
	 * Set the Title of the Creature. Concatens it if length > 16.
	 * @param value The String to test.
	 */
	public void setTitle(String value)
	{
		if (value == null)
			_title = "";
		else if (value.length() > 16)
			_title = value.substring(0, 15);
		else
			_title = value;
	}
	
	/** Set the Creature movement type to walk and send Server->Client packet ChangeMoveType to all others Player. */
	public final void setWalking()
	{
		if (isRunning())
			setIsRunning(false);
	}
	
	/**
	 * Task lauching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send ActionFailed (if attacker is a Player)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 * </ul>
	 */
	class HitTask implements Runnable
	{
		Creature _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		boolean _soulshot;
		
		public HitTask(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
		}
	}
	
	/** Task lauching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		WorldObject[] targets;
		L2Skill skill;
		int hitTime;
		int coolTime;
		int phase;
		boolean simultaneously;
		
		public MagicUseTask(WorldObject[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous)
		{
			targets = tgts;
			skill = s;
			phase = 1;
			hitTime = hit;
			coolTime = coolT;
			simultaneously = simultaneous;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (phase)
				{
					case 1:
						onMagicLaunchedTimer(this);
						break;
					case 2:
						onMagicHitTimer(this);
						break;
					case 3:
						onMagicFinalizer(this);
						break;
					default:
						break;
				}
			}
			catch (Exception e)
			{
				LOGGER.error("Failed executing MagicUseTask on phase {} for skill {}.", e, phase, (skill == null) ? "not found" : skill.getName());
				
				if (simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
			}
		}
	}
	
	/** Task launching the function useMagic() */
	private static class QueuedMagicUseTask implements Runnable
	{
		private final Player _player;
		private final L2Skill _skill;
		private final boolean _isCtrlPressed;
		private final boolean _isShiftPressed;
		
		public QueuedMagicUseTask(Player player, L2Skill skill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_player = player;
			_skill = skill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}
		
		@Override
		public void run()
		{
			try
			{
				_player.useMagic(_skill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't process magic use for {}, skillId {}.", e, (_player == null) ? "noname" : _player.getName(), (_skill == null) ? "not found" : _skill.getId());
			}
		}
	}
	
	/** Task lauching the magic skill phases */
	private class FlyToLocationTask implements Runnable
	{
		private final WorldObject _tgt;
		private final Creature _actor;
		private final L2Skill _skill;
		
		public FlyToLocationTask(Creature actor, WorldObject target, L2Skill skill)
		{
			_actor = actor;
			_tgt = target;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			broadcastPacket(new FlyToLocation(_actor, _tgt, FlyType.valueOf(_skill.getFlyType())));
			setXYZ(_tgt.getX(), _tgt.getY(), _tgt.getZ());
		}
	}
	
	// =========================================================
	/** Map 32 bits (0x0000) containing all abnormal effect in progress */
	private int _AbnormalEffects;
	
	protected CharEffectList _effects = new CharEffectList(this);
	
	// Method - Public
	/**
	 * Launch and add L2Effect (including Stack Group management) to Creature and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a Creature at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a Creature, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Add the L2Effect to the Creature _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the Creature (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the Creature (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icones on player client</li>
	 * </ul>
	 * @param newEffect
	 */
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from Creature and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a Creature at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a Creature, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Remove Func added by this effect from the Creature Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the Creature</li>
	 * <li>Update active skills in progress icones on player client</li>
	 * </ul>
	 * @param effect
	 */
	public final void removeEffect(L2Effect effect)
	{
		_effects.queueEffect(effect, true);
	}
	
	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the Creature.<BR>
	 * <BR>
	 */
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	/**
	 * Confused
	 */
	public final void startConfused()
	{
		getAI().notifyEvent(AiEventType.CONFUSED);
		updateAbnormalEffect();
	}
	
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.CONFUSION);
		else
			removeEffect(effect);
		
		if (!(this instanceof Player))
			getAI().notifyEvent(AiEventType.THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Fake Death
	 */
	public final void startFakeDeath()
	{
		if (!(this instanceof Player))
			return;
		
		((Player) this).setIsFakeDeath(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	public final void stopFakeDeath(boolean removeEffects)
	{
		if (!(this instanceof Player))
			return;
		
		final Player player = ((Player) this);
		
		if (removeEffects)
			stopEffects(L2EffectType.FAKE_DEATH);
		
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		player.setIsFakeDeath(false);
		player.setRecentFakeDeath();
		
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
		broadcastPacket(new Revive(this));
		
		// Schedule a paralyzed task to wait for the animation to finish
		ThreadPool.schedule(() -> setIsParalyzed(false), (int) (2000 / getStat().getMovementSpeedMultiplier()));
		setIsParalyzed(true);
	}
	
	/**
	 * Fear
	 */
	public final void startFear()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.AFRAID);
		updateAbnormalEffect();
	}
	
	public final void stopFear(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.FEAR);
		updateAbnormalEffect();
	}
	
	/**
	 * ImmobileUntilAttacked
	 */
	public final void startImmobileUntilAttacked()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.SLEEPING);
		updateAbnormalEffect();
	}
	
	public final void stopImmobileUntilAttacked(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.IMMOBILEUNTILATTACKED);
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getId());
		}
		
		getAI().notifyEvent(AiEventType.THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Muted
	 */
	public final void startMuted()
	{
		abortCast();
		getAI().notifyEvent(AiEventType.MUTED);
		updateAbnormalEffect();
	}
	
	public final void stopMuted(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.MUTE);
		
		updateAbnormalEffect();
	}
	
	/**
	 * Paralize
	 */
	public final void startParalyze()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.PARALYZED);
	}
	
	public final void stopParalyze()
	{
		if (!(this instanceof Player))
			getAI().notifyEvent(AiEventType.THINK);
	}
	
	/**
	 * PsychicalMuted
	 */
	public final void startPhysicalMuted()
	{
		getAI().notifyEvent(AiEventType.MUTED);
		updateAbnormalEffect();
	}
	
	public final void stopPhysicalMuted(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		
		updateAbnormalEffect();
	}
	
	/**
	 * Root
	 */
	public final void startRooted()
	{
		stopMove(null);
		getAI().notifyEvent(AiEventType.ROOTED);
		updateAbnormalEffect();
	}
	
	public final void stopRooting(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.ROOT);
		
		if (!(this instanceof Player))
			getAI().notifyEvent(AiEventType.THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Sleep
	 */
	public final void startSleeping()
	{
		/* Aborts any attacks/casts if slept */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.SLEEPING);
		updateAbnormalEffect();
	}
	
	public final void stopSleeping(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.SLEEP);
		
		if (!(this instanceof Player))
			getAI().notifyEvent(AiEventType.THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stun
	 */
	public final void startStunning()
	{
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(AiEventType.STUNNED);
		
		if (!(this instanceof Summon))
			getAI().setIntention(IntentionType.IDLE);
		
		updateAbnormalEffect();
	}
	
	public final void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.STUN);
		
		if (!(this instanceof Player))
			getAI().notifyEvent(AiEventType.THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param skillId The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2SkillType and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param skillType The L2SkillType of the L2Effect to remove from _effects
	 * @param negateLvl
	 */
	public final void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		_effects.stopSkillEffects(skillType, negateLvl);
	}
	
	public final void stopSkillEffects(L2SkillType skillType)
	{
		_effects.stopSkillEffects(skillType, -1);
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the Creature and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Remove Func added by this effect from the Creature Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the Creature</li>
	 * <li>Update active skills in progress icones on player client</li>
	 * </ul>
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set. Called on any action except movement (attack, cast).
	 */
	public final void stopEffectsOnAction()
	{
		_effects.stopEffectsOnAction();
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set. Called on decreasing HP and mana burn.
	 * @param awake
	 */
	public final void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}
	
	/**
	 * Broadcast packet related to state of abnormal effect.
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR>
	 * <BR>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	/**
	 * Updates Effect Icons for this character(palyer/summon) and his party if any<BR>
	 * Overridden in:
	 * <ul>
	 * <li>Player</li>
	 * <li>L2Summon</li>
	 * </ul>
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// overridden
	}
	
	/**
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.
	 * @return a map of 16 bits (0x0000) containing all abnormal effect in progress for this Creature.
	 */
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (isStunned())
			ae |= AbnormalEffect.STUN.getMask();
		if (isRooted())
			ae |= AbnormalEffect.ROOT.getMask();
		if (isSleeping())
			ae |= AbnormalEffect.SLEEP.getMask();
		if (isConfused())
			ae |= AbnormalEffect.FEAR.getMask();
		if (isAfraid())
			ae |= AbnormalEffect.FEAR.getMask();
		if (isMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isPhysicalMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isImmobileUntilAttacked())
			ae |= AbnormalEffect.FLOATING_ROOT.getMask();
		
		return ae;
	}
	
	/**
	 * Return all active skills effects in progress on the Creature.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR>
	 * <BR>
	 * @return A table containing all active skills effect in progress on the Creature
	 */
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}
	
	/**
	 * Return L2Effect in progress on the Creature corresponding to the L2Skill Identifier.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in <B>_effects</B>.
	 * @param skillId The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}
	
	/**
	 * Return the first L2Effect in progress on the Creature created by the L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in <B>_effects</B>.
	 * @param skill The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}
	
	/**
	 * Return the first L2Effect in progress on the Creature corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}
	
	/**
	 * This class group all mouvement data.<BR>
	 * <BR>
	 * <B><U> Data</U> :</B>
	 * <ul>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li>
	 * </ul>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public long _moveStartTime;
		public long _moveTimestamp; // last update
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<Location> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	/** Table containing all skillId that are disabled */
	private final Map<Integer, Long> _disabledSkills = new ConcurrentHashMap<>();
	private boolean _allSkillsDisabled;
	
	/** Movement data of this Creature */
	protected MoveData _move;
	
	/** WorldObject targeted by the Creature */
	private WorldObject _target;
	
	// set by the start of attack, in game ticks
	private long _attackEndTime;
	private long _disableBowAttackEndTime;
	private long _castInterruptTime;
	
	protected CreatureAI _ai;
	
	/** Future Skill Cast */
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	
	/**
	 * Add a Func to the Calculator set of the Creature.
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFunc(Func f)
	{
		if (f == null)
			return;
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		
		synchronized (_calculators)
		{
			if (_calculators[stat] == null)
				_calculators[stat] = new Calculator();
			
			// Add the Func to the calculator corresponding to the state
			_calculators[stat].addFunc(f);
		}
	}
	
	/**
	 * Add a list of Funcs to the Calculator set of the Creature.
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFuncs(List<Func> funcs)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the Creature.
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final void removeStatsByOwner(Object owner)
	{
		List<Stats> modifiedStats = null;
		
		int i = 0;
		// Go through the Calculator set
		synchronized (_calculators)
		{
			for (Calculator calc : _calculators)
			{
				if (calc != null)
				{
					// Delete all Func objects of the selected owner
					if (modifiedStats != null)
						modifiedStats.addAll(calc.removeOwner(owner));
					else
						modifiedStats = calc.removeOwner(owner);
					
					if (calc.size() == 0)
						_calculators[i] = null;
				}
				i++;
			}
			
			if (owner instanceof L2Effect)
			{
				if (!((L2Effect) owner).preventExitUpdate)
					broadcastModifiedStats(modifiedStats);
			}
			else
				broadcastModifiedStats(modifiedStats);
		}
	}
	
	private void broadcastModifiedStats(List<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
			return;
		
		boolean broadcastFull = false;
		StatusUpdate su = null;
		
		if (this instanceof Summon && ((Summon) this).getOwner() != null)
			((Summon) this).updateAndBroadcastStatusAndInfos(1);
		else
		{
			for (Stats stat : stats)
			{
				if (stat == Stats.POWER_ATTACK_SPEED)
				{
					if (su == null)
						su = new StatusUpdate(this);
					
					su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
				}
				else if (stat == Stats.MAGIC_ATTACK_SPEED)
				{
					if (su == null)
						su = new StatusUpdate(this);
					
					su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
				}
				else if (stat == Stats.MAX_HP && this instanceof Attackable)
				{
					if (su == null)
						su = new StatusUpdate(this);
					
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				}
				else if (stat == Stats.RUN_SPEED)
					broadcastFull = true;
			}
		}
		
		if (this instanceof Player)
		{
			if (broadcastFull)
				((Player) this).updateAndBroadcastStatus(2);
			else
			{
				((Player) this).updateAndBroadcastStatus(1);
				if (su != null)
					broadcastPacket(su);
			}
		}
		else if (this instanceof Npc)
		{
			if (broadcastFull)
			{
				for (Player player : getKnownType(Player.class))
				{
					if (getMoveSpeed() == 0)
						player.sendPacket(new ServerObjectInfo((Npc) this, player));
					else
						player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) this, player));
				}
			}
			else if (su != null)
				broadcastPacket(su);
		}
		else if (su != null)
			broadcastPacket(su);
	}
	
	public final int getXdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._xDestination;
		
		return getX();
	}
	
	public final int getYdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._yDestination;
		
		return getY();
	}
	
	public final int getZdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._zDestination;
		
		return getZ();
	}
	
	/**
	 * @return True if the Creature is in combat.
	 */
	public boolean isInCombat()
	{
		return hasAI() && AttackStanceTaskManager.getInstance().isInAttackStance(this);
	}
	
	/**
	 * @return True if the Creature is moving.
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * @return True if the Creature is travelling a calculated path.
	 */
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
			return false;
		
		if (m.onGeodataPathIndex == -1)
			return false;
		
		if (m.onGeodataPathIndex == m.geoPath.size() - 1)
			return false;
		
		return true;
	}
	
	/**
	 * @return True if the Creature is casting.
	 */
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}
	
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}
	
	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}
	
	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}
	
	/**
	 * @return True if the cast of the Creature can be aborted.
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}
	
	/**
	 * @return True if the Creature is attacking.
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}
	
	/**
	 * Abort the attack of the Creature and send Server->Client ActionFailed packet.
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
			sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			Future<?> future = _skillCast;
			// cancels the skill hit scheduled task
			if (future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if (future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			
			if (getFusionSkill() != null)
				getFusionSkill().onCastAbort();
			
			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();
			
			if (_allSkillsDisabled)
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
				
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			
			if (this instanceof Playable)
				getAI().notifyEvent(AiEventType.FINISH_CASTING); // setting back previous intention
				
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the Creature during a movement and return True if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the Creature. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the Creature position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why,
	 * client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the Creature position is automatically set to the destination position even if the movement is not finished.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR>
	 * <BR>
	 * @return True if the movement is finished
	 */
	public boolean updatePosition()
	{
		// Get movement data
		MoveData m = _move;
		
		if (m == null)
			return true;
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}
		
		// get current time
		final long time = System.currentTimeMillis();
		
		// Check if the position has already been calculated
		if (m._moveTimestamp > time)
			return false;
		
		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations
		
		double dx = m._xDestination - m._xAccurate;
		double dy = m._yDestination - m._yAccurate;
		double dz;
		
		final boolean isFloating = isFlying() || isInsideZone(ZoneId.WATER);
		
		// Z coordinate will follow geodata or client values once a second to reduce possible cpu load
		if (!isFloating && !m.disregardingGeodata && Rnd.get(10) == 0 && GeoEngine.getInstance().hasGeo(xPrev, yPrev))
		{
			short geoHeight = GeoEngine.getInstance().getHeight(xPrev, yPrev, zPrev);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (this instanceof Player && Math.abs(((Player) this).getClientZ() - geoHeight) > 200 && Math.abs(((Player) this).getClientZ() - geoHeight) < 1500)
			{
				// allow diff
				dz = m._zDestination - zPrev;
			}
			// allow mob to climb up to pcinstance
			else if (isInCombat() && Math.abs(dz) > 200 && (dx * dx + dy * dy) < 40000)
			{
				// climbing
				dz = m._zDestination - zPrev;
			}
			else
				zPrev = geoHeight;
		}
		else
			dz = m._zDestination - zPrev;
		
		double delta = dx * dx + dy * dy;
		// close enough, allows error between client and server geodata if it cannot be avoided
		// should not be applied on vertical movements in water or during flight
		if (delta < 10000 && (dz * dz > 2500) && !isFloating)
			delta = Math.sqrt(delta);
		else
			delta = Math.sqrt(delta + dz * dz);
		
		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed = (getStat().getMoveSpeed() * (time - m._moveTimestamp)) / 1000;
			distFraction = distPassed / delta;
		}
		
		// already there, Set the position of the Creature to the destination
		if (distFraction > 1)
			setXYZ(m._xDestination, m._yDestination, m._zDestination);
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			// Set the position of the Creature to estimated after parcial move
			setXYZ((int) (m._xAccurate), (int) (m._yAccurate), zPrev + (int) (dz * distFraction + 0.5));
		}
		revalidateZone(false);
		
		// Set the timer of last position update to now
		m._moveTimestamp = time;
		
		return (distFraction > 1);
	}
	
	public void revalidateZone(boolean force)
	{
		if (getRegion() == null)
			return;
		
		// This function is called too often from movement code
		if (force)
			_zoneValidateCounter = 4;
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
				_zoneValidateCounter = 4;
			else
				return;
		}
		getRegion().revalidateZones(this);
	}
	
	/**
	 * Stop movement of the Creature (called by AI Accessor only).
	 * <ul>
	 * <li>Delete movement data of the Creature</li>
	 * <li>Set the current position and refresh the region if necessary</li>
	 * </ul>
	 * @param loc : The SpawnLocation where the character must stop.
	 */
	public void stopMove(SpawnLocation loc)
	{
		// Delete movement data of the Creature
		_move = null;
		
		// Set the current position and refresh the region if necessary.
		if (loc != null)
		{
			setXYZ(loc);
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
	}
	
	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	/**
	 * Target an object. If the object is invisible, we set it to null.<br>
	 * <B><U>Overridden in Player</U></B> : Remove the Player from the old target _statusListener and add it to the new target if it was a Creature
	 * @param object WorldObject to target
	 */
	public void setTarget(WorldObject object)
	{
		if (object != null && !object.isVisible())
			object = null;
		
		_target = object;
	}
	
	/**
	 * @return the identifier of the WorldObject targeted or -1.
	 */
	public final int getTargetId()
	{
		return (_target != null) ? _target.getObjectId() : -1;
	}
	
	/**
	 * @return the WorldObject targeted or null.
	 */
	public final WorldObject getTarget()
	{
		return _target;
	}
	
	/**
	 * Calculate movement data for a move to location action and add the Creature to movingObjects of GameTimeController (only called by AI Accessor).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the Creature. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * All Creature in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those Creature each 0.1s.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get current position of the Creature</li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset</li>
	 * <li>Create and Init a MoveData object</li>
	 * <li>Set the Creature _move object to MoveData object</li>
	 * <li>Add the Creature to movingObjects of the GameTimeController</li>
	 * <li>Create a task to notify the AI that Creature arrives at a check point of the movement</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/MoveToLocation </B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B>
	 * <ul>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(WorldObject), onIntentionInteract(WorldObject)</li>
	 * <li>FollowTask</li>
	 * </ul>
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the Creature targeted
	 */
	public void moveToLocation(int x, int y, int z, int offset)
	{
		// get movement speed of character
		double speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
			return;
		
		// get current position of character
		final int curX = getX();
		final int curY = getY();
		final int curZ = getZ();
		
		// calculate distance (dx, dy, dz) between current position and new destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt(dx * dx + dy * dy);
		
		// check vertical movement
		final boolean verticalMovementOnly = isFlying() && distance == 0 && dz != 0;
		if (verticalMovementOnly)
			distance = Math.abs(dz);
			
		// TODO: really necessary?
		// adjust target XYZ when swiming in water (can be easily over 3000)
		if (isInsideZone(ZoneId.WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.sqrt(dx * dx + dy * dy);
		}
		
		double cos;
		double sin;
		
		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
				offset = 5;
			
			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0)
			{
				// Notify the AI that the Creature is arrived at destination
				getAI().notifyEvent(AiEventType.ARRIVED);
				return;
			}
			
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= (offset - 5); // due to rounding error, we have to move a bit closer to be in range
			
			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}
		
		// get new MoveData
		MoveData newMd = new MoveData();
		
		// initialize new MoveData
		newMd.onGeodataPathIndex = -1;
		newMd.disregardingGeodata = false;
		
		// flying chars not checked - even canSeeTarget doesn't work yet
		// swimming also not checked unless in siege zone - but distance is limited
		// npc walkers not checked
		if (!isFlying() && (!isInsideZone(ZoneId.WATER) || isInsideZone(ZoneId.SIEGE)) && !(this instanceof Walker))
		{
			final boolean isInBoat = this instanceof Player && ((Player) this).getBoat() != null;
			if (isInBoat)
				newMd.disregardingGeodata = true;
			
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - World.WORLD_X_MIN) >> 4;
			int gty = (originalY - World.WORLD_Y_MIN) >> 4;
			
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			// assuming intention_follow only when following owner
			if ((!(this instanceof Attackable && ((Attackable) this).isReturningToSpawnPoint())) || (this instanceof Player && !(isInBoat && distance > 1500)) || (this instanceof Summon && !(getAI().getDesire().getIntention() == IntentionType.FOLLOW)) || isAfraid() || this instanceof RiftInvader)
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return;
						
						_move.onGeodataPathIndex = -1; // Set not on geodata path
					}
					catch (NullPointerException e)
					{
						// nothing
					}
				}
				
				if (curX < World.WORLD_X_MIN || curX > World.WORLD_X_MAX || curY < World.WORLD_Y_MIN || curY > World.WORLD_Y_MAX)
				{
					// Temporary fix for character outside world region errors
					getAI().setIntention(IntentionType.IDLE);
					
					if (this instanceof Player)
						((Player) this).logout(false);
					else if (this instanceof Summon)
						return; // prevention when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					else
						onDecay();
					
					return;
				}
				
				// location different if destination wasn't reached (or just z coord is different)
				Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(curX, curY, curZ, x, y, z);
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
			}
			
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (originalDistance - distance > 30 && distance < 2000 && !isAfraid())
			{
				// Path calculation -- overrides previous movement check
				if ((this instanceof Playable && !isInBoat) || isMinion() || isInCombat())
				{
					newMd.geoPath = GeoEngine.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, this instanceof Playable);
					if (newMd.geoPath == null || newMd.geoPath.size() < 2)
					{
						// No path found
						// Even though there's no path found (remember geonodes aren't perfect), the mob is attacking and right now we set it so that the mob will go after target anyway, is dz is small enough.
						// With cellpathfinding this approach could be changed but would require taking off the geonodes and some more checks.
						// Summons will follow their masters no matter what.
						// Currently minions also must move freely since L2AttackableAI commands them to move along with their leader
						if (this instanceof Player || (!(this instanceof Playable) && !isMinion() && Math.abs(z - curZ) > 140) || (this instanceof Summon && !((Summon) this).getFollowStatus()))
							return;
						
						newMd.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
					}
					else
					{
						newMd.onGeodataPathIndex = 0; // on first segment
						newMd.geoPathGtx = gtx;
						newMd.geoPathGty = gty;
						newMd.geoPathAccurateTx = originalX;
						newMd.geoPathAccurateTy = originalY;
						
						x = newMd.geoPath.get(newMd.onGeodataPathIndex).getX();
						y = newMd.geoPath.get(newMd.onGeodataPathIndex).getY();
						z = newMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
						
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			
			// If no distance to go through, the movement is canceled
			if (distance < 1)
			{
				if (this instanceof Summon)
					((Summon) this).setFollowStatus(false);
				
				getAI().setIntention(IntentionType.IDLE);
				return;
			}
		}
		
		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying() || isInsideZone(ZoneId.WATER)) && !verticalMovementOnly)
			distance = Math.sqrt(distance * distance + dz * dz);
		
		// Caclulate the Nb of ticks between the current position and the destination
		newMd._xDestination = x;
		newMd._yDestination = y;
		newMd._zDestination = z;
		
		// Calculate and set the heading of the Creature
		newMd._heading = 0;
		
		newMd._moveStartTime = System.currentTimeMillis();
		
		// set new MoveData as character MoveData
		_move = newMd;
		
		// Does not broke heading on vertical movements
		if (!verticalMovementOnly)
			getPosition().setHeading(MathUtil.calculateHeadingFrom(cos, sin));
		
		// add the character to moving objects of the GameTimeController
		MovementTaskManager.getInstance().add(this);
	}
	
	public boolean moveToNextRoutePoint()
	{
		// character is not on geodata path, return
		if (!isOnGeodataPath())
		{
			_move = null;
			return false;
		}
		
		// character movement is not allowed, return
		if (getStat().getMoveSpeed() <= 0 || isMovementDisabled())
		{
			_move = null;
			return false;
		}
		
		// get current MoveData
		MoveData oldMd = _move;
		
		// get new MoveData
		MoveData newMd = new MoveData();
		
		// initialize new MoveData
		newMd.onGeodataPathIndex = oldMd.onGeodataPathIndex + 1;
		newMd.geoPath = oldMd.geoPath;
		newMd.geoPathGtx = oldMd.geoPathGtx;
		newMd.geoPathGty = oldMd.geoPathGty;
		newMd.geoPathAccurateTx = oldMd.geoPathAccurateTx;
		newMd.geoPathAccurateTy = oldMd.geoPathAccurateTy;
		
		if (oldMd.onGeodataPathIndex == oldMd.geoPath.size() - 2)
		{
			newMd._xDestination = oldMd.geoPathAccurateTx;
			newMd._yDestination = oldMd.geoPathAccurateTy;
			newMd._zDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
		}
		else
		{
			newMd._xDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getX();
			newMd._yDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getY();
			newMd._zDestination = oldMd.geoPath.get(newMd.onGeodataPathIndex).getZ();
		}
		
		newMd._heading = 0;
		newMd._moveStartTime = System.currentTimeMillis();
		
		// set new MoveData as character MoveData
		_move = newMd;
		
		// get travel distance
		double dx = (_move._xDestination - super.getX());
		double dy = (_move._yDestination - super.getY());
		double distance = Math.sqrt(dx * dx + dy * dy);
		
		// set character heading
		if (distance != 0)
			getPosition().setHeading(MathUtil.calculateHeadingFrom(dx, dy));
		
		// add the character to moving objects of the GameTimeController
		MovementTaskManager.getInstance().add(this);
		
		// send MoveToLocation packet to known objects
		broadcastPacket(new MoveToLocation(this));
		
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;
		
		if (m == null)
			return true;
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0); // initial value or false
			m._heading = heading;
		}
		
		return result;
	}
	
	/**
	 * Return the squared distance between the current position of the Creature and the given object.
	 * @param object WorldObject
	 * @return the squared distance
	 */
	public final double getDistanceSq(WorldObject object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return the squared distance between the current position of the Creature and the given x, y, z.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return (dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Return the squared plan distance between the current position of the Creature and the given x, y, z.<BR>
	 * (check only x and y, not z)
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return (dx * dx + dy * dy);
	}
	
	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!
	 * @param object the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public final boolean isInsideRadius(WorldObject object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!
	 * @param loc the Location
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public final boolean isInsideRadius(Location loc, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZ, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param radius the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given point.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		if (strictCheck)
		{
			if (checkZ)
				return (dx * dx + dy * dy + dz * dz) < radius * radius;
			
			return (dx * dx + dy * dy) < radius * radius;
		}
		
		if (checkZ)
			return (dx * dx + dy * dy + dz * dz) <= radius * radius;
		
		return (dx * dx + dy * dy) <= radius * radius;
	}
	
	/**
	 * @return True if arrows are available.
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the Creature.
	 * @param addToExp An int value.
	 * @param addToSp An int value.
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * @return the active weapon instance (always equipped in the right hand).
	 */
	public abstract ItemInstance getActiveWeaponInstance();
	
	/**
	 * @return the active weapon item (always equipped in the right hand).
	 */
	public abstract Weapon getActiveWeaponItem();
	
	/**
	 * @return the secondary weapon instance (always equipped in the left hand).
	 */
	public abstract ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * @return the secondary {@link Item} item (always equiped in the left hand).
	 */
	public abstract Item getSecondaryWeaponItem();
	
	/**
	 * @return the type of attack, depending of the worn weapon.
	 */
	public WeaponType getAttackType()
	{
		final Weapon weapon = getActiveWeaponItem();
		return (weapon == null) ? WeaponType.NONE : weapon.getItemType();
	}
	
	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send ActionFailed (if attacker is a Player)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 * </ul>
	 * @param target The Creature targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 */
	protected void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		// Deny the whole process if actor is casting or is in a state he can't attack.
		if (isCastingNow() || cantAttack())
			return;
		
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		if (target == null || isAlikeDead())
		{
			getAI().notifyEvent(AiEventType.CANCEL);
			return;
		}
		
		if ((this instanceof Npc && target.isAlikeDead()) || target.isDead() || (!getKnownType(Creature.class).contains(target) && !(this instanceof Door)))
		{
			getAI().notifyEvent(AiEventType.CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			// Notify target AI
			if (target.hasAI())
				target.getAI().notifyEvent(AiEventType.EVADED, this);
			
			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(this);
			
			if (target instanceof Player)
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(this));
		}
		
		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss);
		
		// Character will be petrified if attacking a raid related object that's more than 8 levels lower
		if (!Config.RAID_DISABLE_CURSE && target.isRaidRelated() && getLevel() > target.getLevel() + 8)
		{
			final L2Skill skill = FrequentSkill.RAID_CURSE2.getSkill();
			if (skill != null)
			{
				// Send visual and skill effects. Caster is the victim.
				broadcastPacket(new MagicSkillUse(this, this, skill.getId(), skill.getLevel(), 300, 0));
				skill.getEffects(this, this);
			}
			
			damage = 0; // prevents messing up drop calculation
		}
		
		if (!miss && damage > 0)
		{
			// If the target is a player, start AutoAttack
			if (target instanceof Player)
				target.getAI().startAttackStance();
			
			boolean isBow = (getAttackType() == WeaponType.BOW);
			int reflectedDamage = 0;
			
			// Reflect damage system - do not reflect if weapon is a bow or target is invulnerable
			if (!isBow && !target.isInvul())
			{
				// quick fix for no drop from raid if boss attack high-level char with damage reflection
				if (!target.isRaidRelated() || getActingPlayer() == null || getActingPlayer().getLevel() <= target.getLevel() + 8)
				{
					// Calculate reflection damage to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					if (reflectPercent > 0)
					{
						reflectedDamage = (int) (reflectPercent / 100. * damage);
						
						// You can't kill someone from a reflect. If value > current HPs, make damages equal to current HP - 1.
						int currentHp = (int) getCurrentHp();
						if (reflectedDamage >= currentHp)
							reflectedDamage = currentHp - 1;
					}
				}
			}
			
			// Reduce target HPs
			target.reduceCurrentHp(damage, this, null);
			
			// Reduce attacker HPs in case of a reflect.
			if (reflectedDamage > 0)
				reduceCurrentHp(reflectedDamage, target, true, false, null);
			
			if (!isBow) // Do not absorb if weapon is of type bow
			{
				// Absorb HP from the damage inflicted
				double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
				
				if (absorbPercent > 0)
				{
					int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
					int absorbDamage = (int) (absorbPercent / 100. * damage);
					
					if (absorbDamage > maxCanAbsorb)
						absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						
					if (absorbDamage > 0)
						setCurrentHp(getCurrentHp() + absorbDamage);
				}
			}
			
			// Manage cast break of the target (calculating rate, sending message...)
			Formulas.calcCastBreak(target, damage);
			
			// Maybe launch chance skills on us
			if (_chanceSkills != null)
			{
				_chanceSkills.onHit(target, false, crit);
				
				// Reflect triggers onHit
				if (reflectedDamage > 0)
					_chanceSkills.onHit(target, true, false);
			}
			
			// Maybe launch chance skills on target
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onHit(this, true, crit);
		}
		
		// Launch weapon Special ability effect if available
		final Weapon activeWeapon = getActiveWeaponItem();
		if (activeWeapon != null)
			activeWeapon.getSkillEffects(this, target, crit);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature.
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			abortAttack();
			
			if (this instanceof Player)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature.
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (this instanceof Player)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
		}
	}
	
	/**
	 * Reduce the arrow number of the Creature.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>Player</li><BR>
	 * <BR>
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothing
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		if (isInsidePeaceZone(player, this))
		{
			// If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE ActionFailed
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget() instanceof Playable)
		{
			Player target = player.getTarget().getActingPlayer();
			if (target == null || (target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId())))
			{
				// if Player is in Olympia and the match isn't already start, send ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (player.getTarget() != null && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack())
		{
			// If target is not attackable, send ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			// If target is confused, send ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check or dz > 1000
		if (!GeoEngine.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Notify AI with ATTACK
		player.getAI().setIntention(IntentionType.ATTACK, this);
	}
	
	/**
	 * Check if the {@link Player} given as argument can interact with this {@link Creature}.
	 * @param player : The Player to test.
	 * @return true if the Player can interact with the Creature.
	 */
	public boolean canInteract(Player player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;
		
		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;
		
		// Can't interact sitted.
		if (player.isSitting())
			return false;
		
		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;
		
		// Can't interact if regular distance doesn't match.
		if (!isInsideRadius(player, Npc.INTERACTION_DISTANCE, true, false))
			return false;
		
		return true;
	}
	
	public static boolean isInsidePeaceZone(Creature attacker, WorldObject target)
	{
		if (target == null)
			return false;
		
		if (target instanceof Npc || attacker instanceof Npc)
			return false;
		
		// Summon or player check.
		if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getAccessLevel().allowPeaceAttack())
			return false;
		
		if (Config.KARMA_PLAYER_CAN_BE_KILLED_IN_PZ && target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0)
			return false;
		
		if (target instanceof Creature)
			return target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE);
		
		return (MapRegionData.getTown(target.getX(), target.getY(), target.getZ()) != null || attacker.isInsideZone(ZoneId.PEACE));
	}
	
	/**
	 * @return true if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		final WorldRegion region = getRegion();
		return region != null && region.isActive();
	}
	
	/**
	 * @return True if the Creature has a Party in progress.
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * @return the L2Party object of the Creature.
	 */
	public Party getParty()
	{
		return null;
	}
	
	/**
	 * @param target The target to test.
	 * @param weaponType The weapon type to test.
	 * @return The Attack Speed of the Creature (delay (in milliseconds) before next attack).
	 */
	public int calculateTimeBetweenAttacks(Creature target, WeaponType weaponType)
	{
		switch (weaponType)
		{
			case BOW:
				return 1500 * 345 / getStat().getPAtkSpd();
			
			default:
				return Formulas.calcPAtkSpd(this, target, getStat().getPAtkSpd());
		}
	}
	
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	public void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
			return;
		
		for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
		{
			if (!(trigger instanceof L2Skill))
				continue;
			
			if (((L2Skill) trigger).getId() == id)
				_chanceSkills.remove(trigger);
		}
	}
	
	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if (_chanceSkills == null)
			_chanceSkills = new ChanceSkillList(this);
		
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}
	
	public void removeChanceEffect(EffectChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.remove(effect);
	}
	
	public void onStartChanceEffect()
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onStart();
	}
	
	public void onActionTimeChanceEffect()
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onActionTime();
	}
	
	public void onExitChanceEffect()
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onExit();
	}
	
	/**
	 * By default, return an empty immutable map. This method is overidden on {@link Player}, {@link Summon} and {@link Npc}.
	 * @return the skills list of this {@link Creature}.
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return Collections.emptyMap();
	}
	
	/**
	 * Returns the level of a skill owned by this {@link Creature}.
	 * @param skillId : The skill identifier whose level must be returned.
	 * @return the level of the skill identified by skillId.
	 */
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getSkills().get(skillId);
		return (skill == null) ? 0 : skill.getLevel();
	}
	
	/**
	 * @param skillId : The skill identifier to check.
	 * @return the {@link L2Skill} reference if known by this {@link Creature}, or null.
	 */
	public L2Skill getSkill(int skillId)
	{
		return getSkills().get(skillId);
	}
	
	/**
	 * @param skillId : The skill identifier to check.
	 * @return true if the {@link L2Skill} is known by this {@link Creature}, false otherwise.
	 */
	public boolean hasSkill(int skillId)
	{
		return getSkills().containsKey(skillId);
	}
	
	/**
	 * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this Creature.
	 * @return The number of Buffs affecting this Creature
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}
	
	public int getDanceCount()
	{
		return _effects.getDanceCount();
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Broadcast MagicSkillLaunched packet (to display magic skill animation)</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send StatusUpdate with MP modification to the Player</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with ATTACK</li>
	 * <li>Notify the AI of the Creature with EVT_FINISH_CASTING</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT>
	 * @param mut
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		WorldObject[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			abortCast();
			return;
		}
		
		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				// only AURA-type skills can be cast without target
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_UNDEAD:
					break;
				default:
					abortCast();
					return;
			}
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();
		
		if (targets.length > 0 && escapeRange > 0)
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			List<Creature> targetList = new ArrayList<>(targets.length);
			for (WorldObject target : targets)
			{
				if (target instanceof Creature)
				{
					if (!MathUtil.checkIfInRange(escapeRange, this, target, true))
					{
						_skiprange++;
						continue;
					}
					
					if (skill.getSkillRadius() > 0 && skill.isOffensive() && !GeoEngine.getInstance().canSeeTarget(this, target))
					{
						_skipgeo++;
						continue;
					}
					
					if (skill.isOffensive() && isInsidePeaceZone(this, target))
					{
						_skippeace++;
						continue;
					}
					targetList.add((Creature) target);
				}
			}
			
			if (targetList.isEmpty())
			{
				if (this instanceof Player)
				{
					if (_skiprange > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					else if (_skipgeo > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					else if (_skippeace > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				}
				abortCast();
				return;
			}
			mut.targets = targetList.toArray(new Creature[targetList.size()]);
		}
		
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if ((mut.simultaneously && !isCastingSimultaneouslyNow()) || (!mut.simultaneously && !isCastingNow()) || (isAlikeDead() && !skill.isPotion()))
		{
			// now cancels both, simultaneous and normal
			getAI().notifyEvent(AiEventType.CANCEL);
			return;
		}
		
		mut.phase = 2;
		if (mut.hitTime == 0)
			onMagicHitTimer(mut);
		else
			_skillCast = ThreadPool.schedule(mut, 400);
	}
	
	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		final WorldObject[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			abortCast();
			return;
		}
		
		if (getFusionSkill() != null)
		{
			if (mut.simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			getFusionSkill().onCastAbort();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		final L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (mut.simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		// Go through targets table
		for (WorldObject tgt : targets)
		{
			if (tgt instanceof Playable)
			{
				if (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SEED)
					((Creature) tgt).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				
				if (this instanceof Player && tgt instanceof Summon)
					((Summon) tgt).updateAndBroadcastStatus(1);
			}
		}
		
		StatusUpdate su = new StatusUpdate(this);
		boolean isSendStatus = false;
		
		// Consume MP of the Creature and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		final double mpConsume = getStat().getMpConsume(skill);
		if (mpConsume > 0)
		{
			if (mpConsume > getCurrentMp())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
				abortCast();
				return;
			}
			
			getStatus().reduceMp(mpConsume);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			isSendStatus = true;
		}
		
		// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		final double hpConsume = skill.getHpConsume();
		if (hpConsume > 0)
		{
			if (hpConsume > getCurrentHp())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
				abortCast();
				return;
			}
			
			getStatus().reduceHp(hpConsume, this, true);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			isSendStatus = true;
		}
		
		// Send StatusUpdate with MP modification to the Player
		if (isSendStatus)
			sendPacket(su);
		
		if (this instanceof Player)
		{
			// check for charges
			int charges = ((Player) this).getCharges();
			if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				abortCast();
				return;
			}
			
			// generate charges if any
			if (skill.getNumCharges() > 0)
			{
				if (skill.getMaxCharges() > 0)
					((Player) this).increaseCharges(skill.getNumCharges(), skill.getMaxCharges());
				else
					((Player) this).decreaseCharges(skill.getNumCharges());
			}
		}
		
		// Launch the magic skill in order to calculate its effects
		callSkill(mut.skill, mut.targets);
		
		mut.phase = 3;
		if (mut.hitTime == 0 || mut.coolTime == 0)
			onMagicFinalizer(mut);
		else
		{
			if (mut.simultaneously)
				_skillCast2 = ThreadPool.schedule(mut, mut.coolTime);
			else
				_skillCast = ThreadPool.schedule(mut, mut.coolTime);
		}
	}
	
	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.simultaneously)
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		
		_skillCast = null;
		_castInterruptTime = 0;
		
		setIsCastingNow(false);
		setIsCastingSimultaneouslyNow(false);
		
		final L2Skill skill = mut.skill;
		final WorldObject target = mut.targets.length > 0 ? mut.targets[0] : null;
		
		// Attack target after skill use
		if (skill.nextActionIsAttack() && getTarget() instanceof Creature && getTarget() != this && getTarget() == target && getTarget().isAutoAttackable(this))
		{
			if (getAI().getNextIntention() == null || getAI().getNextIntention().getIntention() != IntentionType.MOVE_TO)
				getAI().setIntention(IntentionType.ATTACK, target);
		}
		
		if (skill.isOffensive() && !(skill.getSkillType() == L2SkillType.UNLOCK) && !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
			getAI().startAttackStance();
		
		// Notify the AI of the Creature with EVT_FINISH_CASTING
		getAI().notifyEvent(AiEventType.FINISH_CASTING);
		
		notifyQuestEventSkillFinished(skill, target);
		
		// If the current character is a summon, refresh _currentPetSkill, otherwise if it's a player, refresh _currentSkill and _queuedSkill.
		if (this instanceof Playable)
		{
			boolean isPlayer = this instanceof Player;
			final Player player = getActingPlayer();
			
			if (isPlayer)
			{
				// Wipe current cast state.
				player.setCurrentSkill(null, false, false);
				
				// Check if a skill is queued.
				final SkillUseHolder queuedSkill = player.getQueuedSkill();
				if (queuedSkill.getSkill() != null)
				{
					ThreadPool.execute(new QueuedMagicUseTask(player, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
					player.setQueuedSkill(null, false, false);
				}
			}
			else
				player.setCurrentPetSkill(null, false, false);
		}
	}
	
	// Quest event ON_SPELL_FINISHED
	protected void notifyQuestEventSkillFinished(L2Skill skill, WorldObject target)
	{
	}
	
	public Map<Integer, Long> getDisabledSkills()
	{
		return _disabledSkills;
	}
	
	/**
	 * Enable a skill (remove it from _disabledSkills of the Creature).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the Creature
	 * @param skill The L2Skill to enable
	 */
	public void enableSkill(L2Skill skill)
	{
		if (skill == null)
			return;
		
		_disabledSkills.remove(skill.getReuseHashCode());
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(L2Skill skill, long delay)
	{
		if (skill == null)
			return;
		
		_disabledSkills.put(skill.getReuseHashCode(), (delay > 10) ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}
	
	/**
	 * Check if a skill is disabled. All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B>.
	 * @param skill The L2Skill to check
	 * @return true if the skill is currently disabled.
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (_disabledSkills.isEmpty())
			return false;
		
		if (skill == null || isAllSkillsDisabled())
			return true;
		
		final int hashCode = skill.getReuseHashCode();
		
		final Long timeStamp = _disabledSkills.get(hashCode);
		if (timeStamp == null)
			return false;
		
		if (timeStamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(hashCode);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Disable all skills (set _allSkillsDisabled to True).
	 */
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set _allSkillsDisabled to False).
	 */
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.
	 * @param skill The L2Skill to use
	 * @param targets The table of WorldObject targets
	 */
	public void callSkill(L2Skill skill, WorldObject[] targets)
	{
		try
		{
			// Check if the toggle skill effects are already in progress on the Creature
			if (skill.isToggle() && getFirstEffect(skill.getId()) != null)
				return;
			
			// Initial checks
			for (WorldObject trg : targets)
			{
				if (!(trg instanceof Creature))
					continue;
				
				// Set some values inside target's instance for later use
				final Creature target = (Creature) trg;
				
				if (this instanceof Playable)
				{
					// Raidboss curse.
					if (!Config.RAID_DISABLE_CURSE)
					{
						boolean isVictimTargetBoss = false;
						
						// If the skill isn't offensive, we check extra things such as target's target.
						if (!skill.isOffensive())
						{
							final WorldObject victimTarget = (target.hasAI()) ? target.getAI().getTarget() : null;
							if (victimTarget != null)
								isVictimTargetBoss = victimTarget instanceof Creature && ((Creature) victimTarget).isRaidRelated() && getLevel() > ((Creature) victimTarget).getLevel() + 8;
						}
						
						// Target must be either a raid type, or if the skill is beneficial it checks the target's target.
						if ((target.isRaidRelated() && getLevel() > target.getLevel() + 8) || isVictimTargetBoss)
						{
							final L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
							if (curse != null)
							{
								// Send visual and skill effects. Caster is the victim.
								broadcastPacket(new MagicSkillUse(this, this, curse.getId(), curse.getLevel(), 300, 0));
								curse.getEffects(this, this);
							}
							return;
						}
					}
					
					// Check if over-hit is possible
					if (skill.isOverhit() && target instanceof Monster)
						((Monster) target).overhitEnabled(true);
				}
				
				switch (skill.getSkillType())
				{
					case COMMON_CRAFT: // Crafting does not trigger any chance skills.
					case DWARVEN_CRAFT:
						break;
					
					default: // Launch weapon Special ability skill effect if available
						if (getActiveWeaponItem() != null && !target.isDead())
						{
							if (this instanceof Player && !getActiveWeaponItem().getSkillEffects(this, target, skill).isEmpty())
								sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED).addSkillName(skill));
						}
						
						// Maybe launch chance skills on us
						if (_chanceSkills != null)
							_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
						
						// Maybe launch chance skills on target
						if (target.getChanceSkills() != null)
							target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
				}
			}
			
			// Launch the magic skill and calculate its effects
			final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);
			
			Player player = getActingPlayer();
			if (player != null)
			{
				for (WorldObject target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof Creature)
					{
						if (skill.isOffensive())
						{
							if (target instanceof Playable)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									((Creature) target).getAI().startAttackStance();
									
									// attack of the own pet does not flag player
									if (player.getSummon() != target)
										player.updatePvPStatus((Creature) target);
								}
							}
							// Add attacker into list
							else if (target instanceof Attackable && skill.getId() != 51)
								((Attackable) target).addAttacker(this);
							
							// notify target AI about the attack
							if (((Creature) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									
									default:
										((Creature) target).getAI().notifyEvent(AiEventType.ATTACKED, this);
								}
							}
						}
						else
						{
							if (target instanceof Player)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) && (((Player) target).getPvpFlag() > 0 || ((Player) target).getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target instanceof Attackable && !((Attackable) target).isGuard())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case UNLOCK_SPECIAL:
									case DELUXE_KEY_UNLOCK:
										break;
									
									default:
										player.updatePvPStatus();
								}
							}
						}
						
						switch (skill.getTargetType())
						{
							case TARGET_CORPSE_MOB:
							case TARGET_AREA_CORPSE_MOB:
								if (((Creature) target).isDead())
									((Npc) target).endDecayTask();
								break;
						}
					}
				}
				
				// Mobs in range 1000 see spell
				for (Npc npcMob : player.getKnownTypeInRadius(Npc.class, 1000))
				{
					final List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
					if (scripts != null)
						for (Quest quest : scripts)
							quest.notifySkillSee(npcMob, player, skill, targets, this instanceof Summon);
				}
			}
			
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					
					default:
						for (WorldObject target : targets)
						{
							// notify target AI about the attack
							if (target instanceof Creature && ((Creature) target).hasAI())
								((Creature) target).getAI().notifyEvent(AiEventType.ATTACKED, this);
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't call skill {}.", e, (skill == null) ? "not found" : skill.getId());
		}
	}
	
	/**
	 * @param target Target to check.
	 * @return True if the Creature is behind the target and can't be seen.
	 */
	public boolean isBehind(Creature target)
	{
		if (target == null)
			return false;
		
		final double maxAngleDiff = 60;
		
		double angleChar = MathUtil.calculateAngleFrom(this, target);
		double angleTarget = MathUtil.convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public boolean isBehindTarget()
	{
		WorldObject target = getTarget();
		if (target instanceof Creature)
			return isBehind((Creature) target);
		
		return false;
	}
	
	/**
	 * @param target Target to check.
	 * @return True if the target is facing the Creature.
	 */
	public boolean isInFrontOf(Creature target)
	{
		if (target == null)
			return false;
		
		final double maxAngleDiff = 60;
		
		double angleTarget = MathUtil.calculateAngleFrom(target, this);
		double angleChar = MathUtil.convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	/**
	 * @param target Target to check.
	 * @param maxAngle The angle to check.
	 * @return true if target is in front of Creature (shield def etc)
	 */
	public boolean isFacing(WorldObject target, int maxAngle)
	{
		if (target == null)
			return false;
		
		double maxAngleDiff = maxAngle / 2;
		double angleTarget = MathUtil.calculateAngleFrom(this, target);
		double angleChar = MathUtil.convertHeadingToDegree(getHeading());
		double angleDiff = angleChar - angleTarget;
		
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public boolean isInFrontOfTarget()
	{
		WorldObject target = getTarget();
		if (target instanceof Creature)
			return isInFrontOf((Creature) target);
		
		return false;
	}
	
	/**
	 * @return the level modifier.
	 */
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	/**
	 * @param target Target to check.
	 * @return a Random Damage in function of the weapon.
	 */
	public final int getRandomDamage(Creature target)
	{
		Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		
		return weaponItem.getRandomDamage();
	}
	
	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}
	
	public long getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	/**
	 * @return the level of the Creature.
	 */
	public abstract int getLevel();
	
	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, Creature target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	// Property - Public
	public final int getCON()
	{
		return getStat().getCON();
	}
	
	public final int getDEX()
	{
		return getStat().getDEX();
	}
	
	public final int getINT()
	{
		return getStat().getINT();
	}
	
	public final int getMEN()
	{
		return getStat().getMEN();
	}
	
	public final int getSTR()
	{
		return getStat().getSTR();
	}
	
	public final int getWIT()
	{
		return getStat().getWIT();
	}
	
	public final int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final int getCriticalHit(Creature target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public final int getEvasionRate(Creature target)
	{
		return getStat().getEvasionRate(target);
	}
	
	public final int getMDef(Creature target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	public final int getPDef(Creature target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	public final int getPAtk(Creature target)
	{
		return getStat().getPAtk(target);
	}
	
	public final int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public final int getMAtk(Creature target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	public final int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	public final int getMCriticalHit(Creature target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public final int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public final double getPAtkAnimals(Creature target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public final double getPAtkDragons(Creature target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public final double getPAtkInsects(Creature target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public final double getPAtkMonsters(Creature target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public final double getPAtkPlants(Creature target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	public final double getPAtkGiants(Creature target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public final double getPAtkMagicCreatures(Creature target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	public final double getPDefAnimals(Creature target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	public final double getPDefDragons(Creature target)
	{
		return getStat().getPDefDragons(target);
	}
	
	public final double getPDefInsects(Creature target)
	{
		return getStat().getPDefInsects(target);
	}
	
	public final double getPDefMonsters(Creature target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	public final double getPDefPlants(Creature target)
	{
		return getStat().getPDefPlants(target);
	}
	
	public final double getPDefGiants(Creature target)
	{
		return getStat().getPDefGiants(target);
	}
	
	public final double getPDefMagicCreatures(Creature target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	public final int getMoveSpeed()
	{
		return (int) getStat().getMoveSpeed();
	}
	
	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(Creature object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, Creature attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	public void reduceCurrentHpByDOT(double i, Creature attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}
	
	public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isChampion() && Config.CHAMPION_HP != 0)
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake, isDOT, false);
		else
			getStatus().reduceHp(i, attacker, awake, isDOT, false);
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	public void removeStatusListener(Creature object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	// Property - Public
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	// =========================================================
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	public boolean isChampion()
	{
		return _champion;
	}
	
	/**
	 * Send system message about damage.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B>
	 * <ul>
	 * <li>Player</li>
	 * <li>Servitor</li>
	 * <li>Pet</li>
	 * </ul>
	 * @param target
	 * @param damage
	 * @param mcrit
	 * @param pcrit
	 * @param miss
	 */
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}
	
	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}
	
	public double getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}
	
	/**
	 * Check if target is affected with special buff
	 * @see CharEffectList#isAffected(L2EffectFlag)
	 * @param flag int
	 * @return boolean
	 */
	public boolean isAffected(L2EffectFlag flag)
	{
		return _effects.isAffected(flag);
	}
	
	/**
	 * Check player max buff count
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.MAX_BUFFS_AMOUNT + getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION);
	}
	
	/**
	 * @return a multiplier based on weapon random damage.
	 */
	public final double getRandomDamageMultiplier()
	{
		Weapon activeWeapon = getActiveWeaponItem();
		int random;
		
		if (activeWeapon != null)
			random = activeWeapon.getRandomDamage();
		else
			random = 5 + (int) Math.sqrt(getLevel());
		
		return (1 + ((double) Rnd.get(0 - random, random) / 100));
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	/**
	 * @return true if the character is located in an arena (aka a PvP zone which isn't a siege).
	 */
	public boolean isInArena()
	{
		return false;
	}
	
	public double getCollisionRadius()
	{
		return getTemplate().getCollisionRadius();
	}
	
	public double getCollisionHeight()
	{
		return getTemplate().getCollisionHeight();
	}
	
	@Override
	public final void setRegion(WorldRegion newRegion)
	{
		// If old region exists.
		if (getRegion() != null)
		{
			// No new region is set, we delete directly from current region zones.
			if (newRegion == null)
				getRegion().removeFromZones(this);
			// If a different region is set, we test old region zones to see if we're still on it or no.
			else if (newRegion != getRegion())
				getRegion().revalidateZones(this);
		}
		
		// Update the zone, send the knownlist.
		super.setRegion(newRegion);
		
		// Revalidate current zone (used instead of "getRegion().revalidateZones(this)" because it's overidden on Player).
		revalidateZone(true);
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		// If object is targeted by the Creature, cancel Attack or Cast
		if (object == getTarget())
			setTarget(null);
	}
}