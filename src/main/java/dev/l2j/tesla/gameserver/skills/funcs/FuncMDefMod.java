package dev.l2j.tesla.gameserver.skills.funcs;

import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;

public class FuncMDefMod extends Func
{
	static final FuncMDefMod _fpa_instance = new FuncMDefMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncMDefMod()
	{
		super(Stats.MAGIC_DEFENCE, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.getCharacter() instanceof Player)
		{
			final Player player = env.getPlayer();
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				env.subValue(5);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				env.subValue(5);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				env.subValue(9);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				env.subValue(9);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				env.subValue(13);
		}
		
		env.mulValue(Formulas.MEN_BONUS[env.getCharacter().getMEN()] * env.getCharacter().getLevelMod());
	}
}