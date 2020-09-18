package dev.l2j.tesla.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExOlympiadUserInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.OlympiadType;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.zone.type.OlympiadStadiumZone;

abstract public class OlympiadGameNormal extends AbstractOlympiadGame
{
	private static final String INSERT_RESULT = "INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)";
	
	protected int _damageP1 = 0;
	protected int _damageP2 = 0;
	
	protected Participant _playerOne;
	protected Participant _playerTwo;
	
	protected OlympiadGameNormal(int id, Participant[] opponents)
	{
		super(id);
		
		_playerOne = opponents[0];
		_playerTwo = opponents[1];
		
		_playerOne.getPlayer().setOlympiadGameId(id);
		_playerTwo.getPlayer().setOlympiadGameId(id);
	}
	
	protected static final Participant[] createListOfParticipants(List<Integer> list)
	{
		if (list == null || list.isEmpty() || list.size() < 2)
			return null;
		
		int playerOneObjectId = 0;
		Player playerOne = null;
		Player playerTwo = null;
		
		while (list.size() > 1)
		{
			playerOneObjectId = list.remove(Rnd.get(list.size()));
			playerOne = World.getInstance().getPlayer(playerOneObjectId);
			if (playerOne == null || !playerOne.isOnline())
				continue;
			
			playerTwo = World.getInstance().getPlayer(list.remove(Rnd.get(list.size())));
			if (playerTwo == null || !playerTwo.isOnline())
			{
				list.add(playerOneObjectId);
				continue;
			}
			
			Participant[] result = new Participant[2];
			result[0] = new Participant(playerOne, 1);
			result[1] = new Participant(playerTwo, 2);
			
			return result;
		}
		return null;
	}
	
	@Override
	public final boolean containsParticipant(int objectId)
	{
		return _playerOne.getObjectId() == objectId || _playerTwo.getObjectId() == objectId;
	}
	
	@Override
	public final void sendOlympiadInfo(Creature player)
	{
		player.sendPacket(new ExOlympiadUserInfo(_playerOne.getPlayer()));
		_playerOne.getPlayer().updateEffectIcons();
		player.sendPacket(new ExOlympiadUserInfo(_playerTwo.getPlayer()));
		_playerTwo.getPlayer().updateEffectIcons();
	}
	
	@Override
	public final void broadcastOlympiadInfo(OlympiadStadiumZone stadium)
	{
		stadium.broadcastPacket(new ExOlympiadUserInfo(_playerOne.getPlayer()));
		_playerOne.getPlayer().updateEffectIcons();
		stadium.broadcastPacket(new ExOlympiadUserInfo(_playerTwo.getPlayer()));
		_playerTwo.getPlayer().updateEffectIcons();
	}
	
	@Override
	protected final void broadcastPacket(L2GameServerPacket packet)
	{
		_playerOne.updatePlayer();
		if (_playerOne.getPlayer() != null)
			_playerOne.getPlayer().sendPacket(packet);
		
		_playerTwo.updatePlayer();
		if (_playerTwo.getPlayer() != null)
			_playerTwo.getPlayer().sendPacket(packet);
	}
	
	@Override
	protected final boolean portPlayersToArena(List<Location> spawns)
	{
		boolean result = true;
		result &= portPlayerToArena(_playerOne, spawns.get(0), _stadiumId);
		result &= portPlayerToArena(_playerTwo, spawns.get(1), _stadiumId);
		
		return result;
	}
	
	@Override
	protected final void removals()
	{
		if (_aborted)
			return;
		
		removals(_playerOne.getPlayer(), true);
		removals(_playerTwo.getPlayer(), true);
	}
	
	@Override
	protected final void buffPlayers()
	{
		if (_aborted)
			return;
		
		buffPlayer(_playerOne.getPlayer());
		buffPlayer(_playerTwo.getPlayer());
	}
	
	@Override
	protected final void healPlayers()
	{
		if (_aborted)
			return;
		
		healPlayer(_playerOne.getPlayer());
		healPlayer(_playerTwo.getPlayer());
	}
	
	@Override
	protected final boolean makeCompetitionStart()
	{
		if (!super.makeCompetitionStart())
			return false;
		
		if (_playerOne.getPlayer() == null || _playerTwo.getPlayer() == null)
			return false;
		
		_playerOne.getPlayer().setOlympiadStart(true);
		_playerTwo.getPlayer().setOlympiadStart(true);
		return true;
	}
	
	@Override
	protected final void cleanEffects()
	{
		if (_playerOne.getPlayer() != null && !_playerOne.isDefecting() && !_playerOne.isDisconnected() && _playerOne.getPlayer().getOlympiadGameId() == _stadiumId)
			cleanEffects(_playerOne.getPlayer());
		
		if (_playerTwo.getPlayer() != null && !_playerTwo.isDefecting() && !_playerTwo.isDisconnected() && _playerTwo.getPlayer().getOlympiadGameId() == _stadiumId)
			cleanEffects(_playerTwo.getPlayer());
	}
	
	@Override
	protected final void portPlayersBack()
	{
		if (_playerOne.getPlayer() != null && !_playerOne.isDefecting() && !_playerOne.isDisconnected())
			portPlayerBack(_playerOne.getPlayer());
		
		if (_playerTwo.getPlayer() != null && !_playerTwo.isDefecting() && !_playerTwo.isDisconnected())
			portPlayerBack(_playerTwo.getPlayer());
	}
	
	@Override
	protected final void playersStatusBack()
	{
		if (_playerOne.getPlayer() != null && !_playerOne.isDefecting() && !_playerOne.isDisconnected() && _playerOne.getPlayer().getOlympiadGameId() == _stadiumId)
			playerStatusBack(_playerOne.getPlayer());
		
		if (_playerTwo.getPlayer() != null && !_playerTwo.isDefecting() && !_playerTwo.isDisconnected() && _playerTwo.getPlayer().getOlympiadGameId() == _stadiumId)
			playerStatusBack(_playerTwo.getPlayer());
	}
	
	@Override
	protected final void clearPlayers()
	{
		_playerOne.setPlayer(null);
		_playerOne = null;
		
		_playerTwo.setPlayer(null);
		_playerTwo = null;
	}
	
	@Override
	protected final void handleDisconnect(Player player)
	{
		if (player.getObjectId() == _playerOne.getObjectId())
			_playerOne.setDisconnection(true);
		else if (player.getObjectId() == _playerTwo.getObjectId())
			_playerTwo.setDisconnection(true);
	}
	
	@Override
	protected final boolean checkBattleStatus()
	{
		if (_aborted)
			return false;
		
		if (_playerOne.getPlayer() == null || _playerOne.isDisconnected())
			return false;
		
		if (_playerTwo.getPlayer() == null || _playerTwo.isDisconnected())
			return false;
		
		return true;
	}
	
	@Override
	protected final boolean haveWinner()
	{
		if (!checkBattleStatus())
			return true;
		
		boolean playerOneLost = true;
		try
		{
			if (_playerOne.getPlayer().getOlympiadGameId() == _stadiumId)
				playerOneLost = _playerOne.getPlayer().isDead();
		}
		catch (Exception e)
		{
			playerOneLost = true;
		}
		
		boolean playerTwoLost = true;
		try
		{
			if (_playerTwo.getPlayer().getOlympiadGameId() == _stadiumId)
				playerTwoLost = _playerTwo.getPlayer().isDead();
		}
		catch (Exception e)
		{
			playerTwoLost = true;
		}
		
		return playerOneLost || playerTwoLost;
	}
	
	@Override
	protected void validateWinner(OlympiadStadiumZone stadium)
	{
		if (_aborted)
			return;
		
		final boolean _pOneCrash = (_playerOne.getPlayer() == null || _playerOne.isDisconnected());
		final boolean _pTwoCrash = (_playerTwo.getPlayer() == null || _playerTwo.isDisconnected());
		
		final int playerOnePoints = _playerOne.getStats().getInteger(POINTS);
		final int playerTwoPoints = _playerTwo.getStats().getInteger(POINTS);
		
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
		if (pointDiff <= 0)
			pointDiff = 1;
		else if (pointDiff > Config.ALT_OLY_MAX_POINTS)
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		
		int points;
		
		// Check for if a player defected before battle started.
		if (_playerOne.isDefecting() || _playerTwo.isDefecting())
		{
			if (_playerOne.isDefecting())
			{
				points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
				removePointsFromParticipant(_playerOne, points);
			}
			
			if (_playerTwo.isDefecting())
			{
				points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
				removePointsFromParticipant(_playerTwo, points);
			}
			return;
		}
		
		// Create results for players if a player crashed
		if (_pOneCrash || _pTwoCrash)
		{
			if (_pTwoCrash && !_pOneCrash)
			{
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(_playerOne.getName()));
				
				_playerOne.updateStat(COMP_WON, 1);
				addPointsToParticipant(_playerOne, pointDiff);
				
				_playerTwo.updateStat(COMP_LOST, 1);
				removePointsFromParticipant(_playerTwo, pointDiff);
				
				rewardParticipant(_playerOne.getPlayer(), getReward());
			}
			else if (_pOneCrash && !_pTwoCrash)
			{
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(_playerTwo.getName()));
				
				_playerTwo.updateStat(COMP_WON, 1);
				addPointsToParticipant(_playerTwo, pointDiff);
				
				_playerOne.updateStat(COMP_LOST, 1);
				removePointsFromParticipant(_playerOne, pointDiff);
				
				rewardParticipant(_playerTwo.getPlayer(), getReward());
			}
			else if (_pOneCrash && _pTwoCrash)
			{
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
				
				_playerOne.updateStat(COMP_LOST, 1);
				removePointsFromParticipant(_playerOne, pointDiff);
				
				_playerTwo.updateStat(COMP_LOST, 1);
				removePointsFromParticipant(_playerTwo, pointDiff);
			}
			
			_playerOne.updateStat(COMP_DONE, 1);
			_playerTwo.updateStat(COMP_DONE, 1);
			
			return;
		}
		
		// Calculate Fight time
		long _fightTime = (System.currentTimeMillis() - _startTime);
		
		double playerOneHp = 0;
		if (_playerOne.getPlayer() != null && !_playerOne.getPlayer().isDead())
		{
			playerOneHp = _playerOne.getPlayer().getCurrentHp() + _playerOne.getPlayer().getCurrentCp();
			if (playerOneHp < 0.5)
				playerOneHp = 0;
		}
		
		double playerTwoHp = 0;
		if (_playerTwo.getPlayer() != null && !_playerTwo.getPlayer().isDead())
		{
			playerTwoHp = _playerTwo.getPlayer().getCurrentHp() + _playerTwo.getPlayer().getCurrentCp();
			if (playerTwoHp < 0.5)
				playerTwoHp = 0;
		}
		
		// if players crashed, search if they've relogged
		_playerOne.updatePlayer();
		_playerTwo.updatePlayer();
		
		if ((_playerOne.getPlayer() == null || !_playerOne.getPlayer().isOnline()) && (_playerTwo.getPlayer() == null || !_playerTwo.getPlayer().isOnline()))
		{
			_playerOne.updateStat(COMP_DRAWN, 1);
			_playerTwo.updateStat(COMP_DRAWN, 1);
			stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
		}
		else if (_playerTwo.getPlayer() == null || !_playerTwo.getPlayer().isOnline() || (playerTwoHp == 0 && playerOneHp != 0) || (_damageP1 > _damageP2 && playerTwoHp != 0 && playerOneHp != 0))
		{
			stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(_playerOne.getName()));
			
			_playerOne.updateStat(COMP_WON, 1);
			_playerTwo.updateStat(COMP_LOST, 1);
			
			addPointsToParticipant(_playerOne, pointDiff);
			removePointsFromParticipant(_playerTwo, pointDiff);
			
			// Save Fight Result
			saveResults(_playerOne, _playerTwo, 1, _startTime, _fightTime, getType());
			rewardParticipant(_playerOne.getPlayer(), getReward());
		}
		else if (_playerOne.getPlayer() == null || !_playerOne.getPlayer().isOnline() || (playerOneHp == 0 && playerTwoHp != 0) || (_damageP2 > _damageP1 && playerOneHp != 0 && playerTwoHp != 0))
		{
			stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(_playerTwo.getName()));
			
			_playerTwo.updateStat(COMP_WON, 1);
			_playerOne.updateStat(COMP_LOST, 1);
			
			addPointsToParticipant(_playerTwo, pointDiff);
			removePointsFromParticipant(_playerOne, pointDiff);
			
			// Save Fight Result
			saveResults(_playerOne, _playerTwo, 2, _startTime, _fightTime, getType());
			rewardParticipant(_playerTwo.getPlayer(), getReward());
		}
		else
		{
			// Save Fight Result
			saveResults(_playerOne, _playerTwo, 0, _startTime, _fightTime, getType());
			
			stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
			
			removePointsFromParticipant(_playerOne, Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
			removePointsFromParticipant(_playerTwo, Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
		}
		
		_playerOne.updateStat(COMP_DONE, 1);
		_playerTwo.updateStat(COMP_DONE, 1);
	}
	
	@Override
	protected final void addDamage(Player player, int damage)
	{
		if (_playerOne.getPlayer() == null || _playerTwo.getPlayer() == null)
			return;
		
		if (player == _playerOne.getPlayer())
			_damageP1 += damage;
		else if (player == _playerTwo.getPlayer())
			_damageP2 += damage;
	}
	
	@Override
	public final String[] getPlayerNames()
	{
		return new String[]
		{
			_playerOne.getName(),
			_playerTwo.getName()
		};
	}
	
	@Override
	public boolean checkDefection()
	{
		_playerOne.updatePlayer();
		_playerTwo.updatePlayer();
		
		SystemMessage reason = checkDefection(_playerOne.getPlayer());
		if (reason != null)
		{
			_playerOne.setDefection(true);
			if (_playerTwo.getPlayer() != null)
				_playerTwo.getPlayer().sendPacket(reason);
		}
		
		reason = checkDefection(_playerTwo.getPlayer());
		if (reason != null)
		{
			_playerTwo.setDefection(true);
			if (_playerOne.getPlayer() != null)
				_playerOne.getPlayer().sendPacket(reason);
		}
		
		return _playerOne.isDefecting() || _playerTwo.isDefecting();
	}
	
	@Override
	public final void resetDamage()
	{
		_damageP1 = 0;
		_damageP2 = 0;
	}
	
	protected static final void saveResults(Participant one, Participant two, int _winner, long _startTime, long _fightTime, OlympiadType type)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_RESULT))
		{
			ps.setInt(1, one.getObjectId());
			ps.setInt(2, two.getObjectId());
			ps.setInt(3, one.getBaseClass());
			ps.setInt(4, two.getBaseClass());
			ps.setInt(5, _winner);
			ps.setLong(6, _startTime);
			ps.setLong(7, _fightTime);
			ps.setInt(8, (type == OlympiadType.CLASSED ? 1 : 0));
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save Olympiad results.", e);
		}
	}
}