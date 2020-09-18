package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.enums.CabalType;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToPawn;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Layane
 */
public class CabalBuffer extends Folk
{
	protected static final String[] MESSAGES_LOSER =
	{
		"%player_cabal_loser%! All is lost! Prepare to meet the goddess of death!",
		"%player_cabal_loser%! You bring an ill wind!",
		"%player_cabal_loser%! You might as well give up!",
		"A curse upon you!",
		"All is lost! Prepare to meet the goddess of death!",
		"All is lost! The prophecy of destruction has been fulfilled!",
		"The prophecy of doom has awoken!",
		"This world will soon be annihilated!"
	};
	
	protected static final String[] MESSAGES_WINNER =
	{
		"%player_cabal_winner%! I bestow on you the authority of the abyss!",
		"%player_cabal_winner%, Darkness shall be banished forever!",
		"%player_cabal_winner%, the time for glory is at hand!",
		"All hail the eternal twilight!",
		"As foretold in the prophecy of darkness, the era of chaos has begun!",
		"The day of judgment is near!",
		"The prophecy of darkness has been fulfilled!",
		"The prophecy of darkness has come to pass!"
	};
	
	private ScheduledFuture<?> _aiTask;
	protected int _step = 0; // Flag used to delay chat broadcast.
	
	public CabalBuffer(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_aiTask = ThreadPool.scheduleAtFixedRate(new CabaleAI(this), 5000, 5000);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Calculate the distance between the Player and the Npc.
			if (!canInteract(player))
			{
				// Notify the Player AI with INTERACT
				player.getAI().setIntention(IntentionType.INTERACT, this);
			}
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAI().setIntention(IntentionType.IDLE);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	/**
	 * For each known player in range, cast either the positive or negative buff. <BR>
	 * The stats affected depend on the player type, either a fighter or a mystic. <BR>
	 * <BR>
	 * Curse of Destruction (Loser)<BR>
	 * - Fighters: -25% Accuracy, -25% Effect Resistance<BR>
	 * - Mystics: -25% Casting Speed, -25% Effect Resistance<BR>
	 * <BR>
	 * Blessing of Prophecy (Winner)<BR>
	 * - Fighters: +25% Max Load, +25% Effect Resistance<BR>
	 * - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<BR>
	 */
	private class CabaleAI implements Runnable
	{
		private final CabalBuffer _caster;
		
		protected CabaleAI(CabalBuffer caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			boolean isBuffAWinner = false;
			boolean isBuffALoser = false;
			
			final CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
			
			// Defines which cabal is the loser.
			CabalType losingCabal = CabalType.NORMAL;
			if (winningCabal == CabalType.DAWN)
				losingCabal = CabalType.DUSK;
			else if (winningCabal == CabalType.DUSK)
				losingCabal = CabalType.DAWN;
			
			// Those lists store players for the shout.
			final List<Player> playersList = new ArrayList<>();
			final List<Player> gmsList = new ArrayList<>();
			
			for (Player player : getKnownTypeInRadius(Player.class, 900))
			{
				if (player.isGM())
					gmsList.add(player);
				else
					playersList.add(player);
				
				final CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
				if (playerCabal == CabalType.NORMAL)
					continue;
				
				if (!isBuffAWinner && playerCabal == winningCabal && _caster.getNpcId() == SevenSignsManager.ORATOR_NPC_ID)
				{
					isBuffAWinner = true;
					handleCast(player, (!player.isMageClass() ? 4364 : 4365));
				}
				else if (!isBuffALoser && playerCabal == losingCabal && _caster.getNpcId() == SevenSignsManager.PREACHER_NPC_ID)
				{
					isBuffALoser = true;
					handleCast(player, (!player.isMageClass() ? 4361 : 4362));
				}
				
				// Buff / debuff only 1 ppl per round.
				if (isBuffAWinner && isBuffALoser)
					break;
			}
			
			// Autochat every 60sec. The actual AI cycle is 5sec, so delay it of 12 steps.
			if (_step >= 12)
			{
				if (!playersList.isEmpty() || !gmsList.isEmpty())
				{
					// Pickup a random message from string arrays.
					String text;
					if (_caster.getCollisionHeight() > 30)
						text = Rnd.get(MESSAGES_LOSER);
					else
						text = Rnd.get(MESSAGES_WINNER);
					
					if (text.indexOf("%player_cabal_winner%") > -1)
					{
						for (Player nearbyPlayer : playersList)
						{
							if (SevenSignsManager.getInstance().getPlayerCabal(nearbyPlayer.getObjectId()) == winningCabal)
							{
								text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());
								break;
							}
						}
					}
					else if (text.indexOf("%player_cabal_loser%") > -1)
					{
						for (Player nearbyPlayer : playersList)
						{
							if (SevenSignsManager.getInstance().getPlayerCabal(nearbyPlayer.getObjectId()) == losingCabal)
							{
								text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());
								break;
							}
						}
					}
					
					if (!text.contains("%player_"))
					{
						CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL, getName(), text);
						
						for (Player nearbyPlayer : playersList)
							nearbyPlayer.sendPacket(cs);
						
						for (Player nearbyGM : gmsList)
							nearbyGM.sendPacket(cs);
					}
				}
				_step = 0;
			}
			else
				_step++;
		}
		
		private void handleCast(Player player, int skillId)
		{
			int skillLevel = (player.getLevel() > 40) ? 1 : 2;
			
			final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				skill.getEffects(_caster, player);
				broadcastPacket(new MagicSkillUse(_caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skillId));
			}
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		super.deleteMe();
	}
}