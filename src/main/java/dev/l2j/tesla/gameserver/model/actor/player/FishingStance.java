package dev.l2j.tesla.gameserver.model.actor.player;

import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.Fish;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.PenaltyMonster;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.FishingChampionshipManager;
import dev.l2j.tesla.gameserver.data.xml.FishData;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.network.serverpackets.ExFishingEnd;
import dev.l2j.tesla.gameserver.network.serverpackets.ExFishingHpRegen;
import dev.l2j.tesla.gameserver.network.serverpackets.ExFishingStart;
import dev.l2j.tesla.gameserver.network.serverpackets.ExFishingStartCombat;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles all fishing aspects and variables.<br>
 * <br>
 * The fishing stance starts with a task named _lookingForFish. This task handles the waiting process, up to the caught time.<br>
 * <br>
 * Once a {@link Fish} is found, a new task occurs, named _fishCombat. The {@link Player} will play a minigame to reduce the resilience of the Fish. If he succeeds, he caught the Fish.
 */
public class FishingStance
{
	private final Player _fisher;
	
	private Location _loc = new Location(0, 0, 0);
	
	private int _time;
	private int _stop;
	private int _goodUse;
	private int _anim;
	private int _mode;
	private int _deceptiveMode;
	
	private Future<?> _lookingForFish;
	private Future<?> _fishCombat;
	
	private boolean _thinking;
	
	private Fish _fish;
	private int _fishCurHp;
	
	private boolean _isUpperGrade;
	
	private ItemInstance _lure;
	private int _lureType;
	
	public FishingStance(Player fisher)
	{
		_fisher = fisher;
	}
	
	/**
	 * @param group : The group based on lure type (beginner, normal, upper grade).
	 * @return a random {@link Fish} type, based on randomness and lure's group.
	 */
	private int getRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
							type = 5;
						else if (check <= 77)
							type = 4;
						else
							type = 6;
						break;
					
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
							type = 4;
						else if (check <= 77)
							type = 6;
						else
							type = 5;
						break;
					
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
							type = 6;
						else if (check <= 77)
							type = 5;
						else
							type = 4;
						break;
					
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
							type = 4;
						else if (check <= 66)
							type = 5;
						else
							type = 6;
						break;
				}
				break;
			
			case 1: // normal fish
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
							type = 1;
						else if (check <= 74)
							type = 0;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
							type = 0;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
							type = 2;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 0;
						else
							type = 3;
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
							type = 0;
						else if (check <= 66)
							type = 1;
						else
							type = 2;
						break;
				}
				break;
			
			case 2: // upper grade fish, luminous lure
				switch (_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
							type = 8;
						else if (check <= 77)
							type = 7;
						else
							type = 9;
						break;
					
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
							type = 7;
						else if (check <= 77)
							type = 9;
						else
							type = 8;
						break;
					
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
							type = 9;
						else if (check <= 77)
							type = 8;
						else
							type = 7;
						break;
					
					case 8485: // prize-winning fishing lure
						if (check <= 33)
							type = 7;
						else if (check <= 66)
							type = 8;
						else
							type = 9;
						break;
				}
		}
		return type;
	}
	
	/**
	 * @return a fish level, based on multiple factors. It never goes out 1-27, and is slightly impacted by randomness.
	 */
	private int getRandomFishLvl()
	{
		// Fisherman potion overrides skill level.
		final L2Effect effect = _fisher.getFirstEffect(2274);
		
		// Also check the presence of Fishing Expertise skill.
		int level = (effect != null) ? (int) effect.getSkill().getPower() : _fisher.getSkillLevel(1315);
		if (level <= 0)
			return 1;
		
		// Random aspect : 35% to get -1 level, 15% to get +1 level.
		final int check = Rnd.get(100);
		if (check < 35)
			level--;
		else if (check < 50)
			level++;
		
		// Return a number from 1 to 27, no matter what.
		return MathUtil.limit(level, 1, 27);
	}
	
	/**
	 * @return the bait {@link Location}.
	 */
	public Location getLoc()
	{
		return _loc;
	}
	
	/**
	 * @return true if currently looking for fish (waiting stance), false otherwise.
	 */
	public boolean isLookingForFish()
	{
		return _lookingForFish != null;
	}
	
	/**
	 * @return true if currently fighting fish (minigame), false otherwise.
	 */
	public boolean isUnderFishCombat()
	{
		return _fishCombat != null;
	}
	
	/**
	 * Modify {@link Fish} current HP during the minigame. We also handle the different events (HP reaching 0, or HP going past maximum HP).
	 * @param hp : The HP amount to add or remove.
	 * @param penalty : The penalty amount.
	 */
	public void changeHp(int hp, int penalty)
	{
		_fishCurHp -= hp;
		if (_fishCurHp < 0)
			_fishCurHp = 0;
		
		_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, penalty, _deceptiveMode));
		_anim = 0;
		
		if (_fishCurHp > _fish.getHp() * 2)
		{
			_fishCurHp = _fish.getHp() * 2;
			end(false);
			return;
		}
		else if (_fishCurHp == 0)
			end(true);
	}
	
	protected void aiTask()
	{
		if (_thinking)
			return;
		
		_thinking = true;
		_time--;
		
		try
		{
			if (_mode == 1)
			{
				if (_deceptiveMode == 0)
					_fishCurHp += _fish.getHpRegen();
			}
			else
			{
				if (_deceptiveMode == 1)
					_fishCurHp += _fish.getHpRegen();
			}
			
			if (_stop == 0)
			{
				_stop = 1;
				int check = Rnd.get(100);
				if (check >= 70)
					_mode = _mode == 0 ? 1 : 0;
				
				if (_isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
				}
			}
			else
				_stop--;
		}
		finally
		{
			_thinking = false;
			if (_anim != 0)
				_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode));
			else
				_fisher.sendPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode));
		}
	}
	
	public void useRealing(int dmg, int penalty)
	{
		_anim = 2;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, penalty);
			return;
		}
		
		if (_fisher == null)
			return;
		
		if (_mode == 1)
		{
			if (_deceptiveMode == 0)
			{
				// Reeling is successful, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (penalty == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
				
				_goodUse = 1;
				changeHp(dmg, penalty);
			}
			else
			{
				// Reeling failed, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, penalty);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				// Reeling failed, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, penalty);
			}
			else
			{
				// Reeling is successful, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (penalty == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
				
				_goodUse = 1;
				changeHp(dmg, penalty);
			}
		}
	}
	
	public void usePomping(int dmg, int penalty)
	{
		_anim = 1;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, penalty);
			return;
		}
		
		if (_fisher == null)
			return;
		
		if (_mode == 0)
		{
			if (_deceptiveMode == 0)
			{
				// Pumping is successful. Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (penalty == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
				
				_goodUse = 1;
				changeHp(dmg, penalty);
			}
			else
			{
				// Pumping failed, Regained: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, penalty);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				// Pumping failed, Regained: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, penalty);
			}
			else
			{
				// Pumping is successful. Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (penalty == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
				
				_goodUse = 1;
				changeHp(dmg, penalty);
			}
		}
	}
	
	/**
	 * Start the fishing process.
	 * <ul>
	 * <li>The {@link Player} is immobilized, if he was moving.</li>
	 * <li>The bait {@link Location} and the lure are set.</li>
	 * <li>The {@link Fish} to caught is calculated.</li>
	 * <li>The _lookingForFish task is processed.</li>
	 * </ul>
	 * @param x : The X bait location.
	 * @param y : The Y bait location.
	 * @param z : The Z bait location.
	 * @param lure : The used lure.
	 */
	public void start(int x, int y, int z, ItemInstance lure)
	{
		if (_fisher.isDead())
			return;
		
		// Stop the player.
		_fisher.stopMove(null);
		_fisher.setIsImmobilized(true);
		
		// Set variables.
		_loc.set(x, y, z);
		_lure = lure;
		
		int group;
		
		switch (_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				group = 0;
				
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				group = 2;
				
			default:
				group = 1;
		}
		
		// Define which fish we gonna catch.
		_fish = FishData.getInstance().getFish(getRandomFishLvl(), getRandomFishType(group), group);
		if (_fish == null)
		{
			end(false);
			return;
		}
		
		_fisher.sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		
		_fisher.broadcastPacket(new ExFishingStart(_fisher, _fish.getType(_lure.isNightLure()), _loc, _lure.isNightLure()));
		_fisher.sendPacket(new PlaySound(1, "SF_P_01"));
		
		// "Looking for fish" task is now processed.
		if (_lookingForFish == null)
		{
			final int lureid = _lure.getItemId();
			final boolean isNoob = _fish.getGroup() == 0;
			final boolean isUpperGrade = _fish.getGroup() == 2;
			
			int checkDelay = 0;
			if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) // low grade
				checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.33)));
			else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613) || (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486)) // medium grade, beginner, prize-winning & quest special bait
				checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.00)));
			else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) // high grade
				checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (0.66)));
			
			final long timer = System.currentTimeMillis() + _fish.getWaitTime() + 10000;
			
			_lookingForFish = ThreadPool.scheduleAtFixedRate(() ->
			{
				if (System.currentTimeMillis() >= timer)
				{
					end(false);
					return;
				}
				
				if (_fish.getType(_lure.isNightLure()) == -1)
					return;
				
				if (_fish.getGuts() > Rnd.get(1000))
				{
					// Stop task.
					if (_lookingForFish != null)
					{
						_lookingForFish.cancel(false);
						_lookingForFish = null;
					}
					
					_fishCurHp = _fish.getHp();
					_time = _fish.getCombatTime() / 1000;
					_isUpperGrade = isUpperGrade;
					_lure = lure;
					
					if (isUpperGrade)
					{
						_deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
						_lureType = 2;
					}
					else
					{
						_deceptiveMode = 0;
						_lureType = isNoob ? 0 : 1;
					}
					_mode = Rnd.get(100) >= 80 ? 1 : 0;
					
					_fisher.broadcastPacket(new ExFishingStartCombat(_fisher, _time, _fish.getHp(), _mode, _lureType, _deceptiveMode));
					_fisher.sendPacket(new PlaySound(1, "SF_S_01"));
					
					// Succeeded in getting a bite
					_fisher.sendPacket(SystemMessageId.GOT_A_BITE);
					
					if (_fishCombat == null)
						_fishCombat = ThreadPool.scheduleAtFixedRate(() ->
						{
							if (_fish == null)
								return;
							
							// The fish got away.
							if (_fishCurHp >= _fish.getHp() * 2)
							{
								_fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
								end(false);
							}
							// Time is up, the fish spit the hook.
							else if (_time <= 0)
							{
								_fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
								end(false);
							}
							else
								aiTask();
						}, 1000, 1000);
				}
			}, 10000, checkDelay);
		}
	}
	
	/**
	 * Ends the fishing process.
	 * <ul>
	 * <li>Process the reward ({@link Fish} or 5% {@link PenaltyMonster}), if "win" is set as True.</li>
	 * <li>Cleanup all variables.</li>
	 * <li>Give back the movement ability to the {@link Player}.</li>
	 * <li>End all running tasks.</li>
	 * </ul>
	 * @param win : If true, a Fish or a PenaltyMonster has been caught.
	 */
	public void end(boolean win)
	{
		if (win)
		{
			if (Rnd.get(100) < 5)
			{
				int npcId = 18319 + Math.min(_fisher.getLevel() / 11, 7); // 18319-18326
				
				PenaltyMonster npc = new PenaltyMonster(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(npcId));
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				npc.spawnMe(_fisher.getPosition());
				npc.setPlayerToKill(_fisher);
				
				_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
			}
			else
			{
				_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
				_fisher.addItem("Fishing", _fish.getId(), 1, null, true);
				
				FishingChampionshipManager.getInstance().newFish(_fisher, _lure.getItemId());
			}
		}
		
		if (_fish == null)
			_fisher.sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		
		// Cleanup variables.
		_time = 0;
		_stop = 0;
		_goodUse = 0;
		_anim = 0;
		_mode = 0;
		_deceptiveMode = 0;
		
		_thinking = false;
		
		_fish = null;
		_fishCurHp = 0;
		
		_isUpperGrade = false;
		
		_lure = null;
		_lureType = 0;
		
		_loc.clean();
		
		// Ends fishing
		_fisher.broadcastPacket(new ExFishingEnd(win, _fisher.getObjectId()));
		_fisher.sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		_fisher.setIsImmobilized(false);
		
		// Stop tasks.
		if (_lookingForFish != null)
		{
			_lookingForFish.cancel(false);
			_lookingForFish = null;
		}
		
		if (_fishCombat != null)
		{
			_fishCombat.cancel(false);
			_fishCombat = null;
		}
	}
}