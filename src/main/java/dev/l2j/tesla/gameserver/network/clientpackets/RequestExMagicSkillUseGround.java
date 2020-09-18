package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.ValidateLocation;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.Location;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	
	private int _skillId;
	
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		
		_skillId = readD();
		
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current player
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = player.getSkill(_skillId);
		if (skill == null)
			return;
		
		player.setCurrentSkillWorldPosition(new Location(_x, _y, _z));
		
		// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
		player.getPosition().setHeading(MathUtil.calculateHeadingFrom(player.getX(), player.getY(), _x, _y));
		player.broadcastPacket(new ValidateLocation(player));
		
		player.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
}