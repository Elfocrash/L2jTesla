package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.enums.skills.AbnormalEffect;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Chest;
import dev.l2j.tesla.gameserver.network.serverpackets.Earthquake;
import dev.l2j.tesla.gameserver.network.serverpackets.ExRedSky;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SSQInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.gameserver.network.serverpackets.SunRise;
import dev.l2j.tesla.gameserver.network.serverpackets.SunSet;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>hide = makes yourself invisible or visible.</li>
 * <li>earthquake = causes an earthquake of a given intensity and duration around you.</li>
 * <li>gmspeed = temporary Super Haste effect.</li>
 * <li>para/unpara = paralyze/remove paralysis from target.</li>
 * <li>para_all/unpara_all = same as para/unpara, affects the whole world.</li>
 * <li>polyself/unpolyself = makes you look as a specified mob.</li>
 * <li>social = forces an Creature instance to broadcast social action packets.</li>
 * <li>effect = forces an Creature instance to broadcast MSU packets.</li>
 * <li>abnormal = force changes over an Creature instance's abnormal state.</li>
 * <li>play_sound/jukebox = Music broadcasting related commands.</li>
 * <li>atmosphere = sky change related commands.</li>
 * </ul>
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_hide",
		"admin_invul",
		"admin_undying",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_social",
		"admin_social_menu",
		"admin_effect",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_jukebox",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_hide"))
		{
			if (!activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.decayMe();
				activeChar.broadcastUserInfo();
				activeChar.spawnMe();
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.equals("admin_invul"))
		{
			WorldObject object = activeChar.getTarget();
			if (object == null)
				object = activeChar;
			
			if (!(object instanceof Creature))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature target = (Creature) object;
			target.setIsInvul(!target.isInvul());
			
			activeChar.sendMessage(target.getName() + ((target.isInvul()) ? " is now invulnerable." : " is now vulnerable."));
		}
		else if (command.equals("admin_undying"))
		{
			WorldObject object = activeChar.getTarget();
			if (object == null)
				object = activeChar;
			
			if (!(object instanceof Creature))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature target = (Creature) object;
			target.setIsMortal(!target.isMortal());
			
			activeChar.sendMessage(target.getName() + ((!target.isMortal()) ? " is now immortal." : " is now mortal."));
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				activeChar.broadcastPacket(new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				
				L2GameServerPacket packet = null;
				
				if (type.equals("ssqinfo"))
				{
					if (state.equals("dawn"))
						packet = SSQInfo.DAWN_SKY_PACKET;
					else if (state.equals("dusk"))
						packet = SSQInfo.DUSK_SKY_PACKET;
					else if (state.equals("red"))
						packet = SSQInfo.RED_SKY_PACKET;
					else if (state.equals("regular"))
						packet = SSQInfo.REGULAR_SKY_PACKET;
				}
				else if (type.equals("sky"))
				{
					if (state.equals("night"))
						packet = SunSet.STATIC_PACKET;
					else if (state.equals("day"))
						packet = SunRise.STATIC_PACKET;
					else if (state.equals("red"))
						packet = new ExRedSky(10);
				}
				else
				{
					activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
					activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
				}
				
				if (packet != null)
					World.toAllOnlinePlayers(packet);
			}
			catch (Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
				activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
			}
		}
		else if (command.startsWith("admin_jukebox"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				final String sound = command.substring(17);
				final PlaySound snd = (sound.contains(".")) ? new PlaySound(sound) : new PlaySound(1, sound);
				
				activeChar.broadcastPacket(snd);
				activeChar.sendMessage("Playing " + sound + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_para_all"))
		{
			for (Player player : activeChar.getKnownType(Player.class))
			{
				if (player.isGM())
					continue;
				
				player.startAbnormalEffect(AbnormalEffect.HOLD_2);
				player.setIsParalyzed(true);
				player.startParalyze();
			}
		}
		else if (command.startsWith("admin_unpara_all"))
		{
			for (Player player : activeChar.getKnownType(Player.class))
			{
				player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
				player.setIsParalyzed(false);
			}
		}
		else if (command.startsWith("admin_para"))
		{
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Creature))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature creature = (Creature) target;
			creature.startAbnormalEffect(AbnormalEffect.HOLD_2);
			creature.setIsParalyzed(true);
			creature.startParalyze();
		}
		else if (command.startsWith("admin_unpara"))
		{
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Creature))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature creature = (Creature) target;
			creature.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			creature.setIsParalyzed(false);
			creature.stopParalyze();
		}
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				activeChar.stopSkillEffects(7029);
				
				final int val = Integer.parseInt(st.nextToken());
				if (val > 0 && val < 5)
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, val));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //gmspeed value (0-4).");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				final int social = Integer.parseInt(st.nextToken());
				
				if (st.hasMoreTokens())
				{
					final String targetOrRadius = st.nextToken();
					if (targetOrRadius != null)
					{
						Player player = World.getInstance().getPlayer(targetOrRadius);
						if (player != null)
						{
							if (performSocial(social, player))
								activeChar.sendMessage(player.getName() + " was affected by your social request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							final int radius = Integer.parseInt(targetOrRadius);
							
							for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
								performSocial(social, object);
							
							activeChar.sendMessage(radius + " units radius was affected by your social request.");
						}
					}
				}
				else
				{
					WorldObject obj = activeChar.getTarget();
					if (obj == null)
						obj = activeChar;
					
					if (performSocial(social, obj))
						activeChar.sendMessage(obj.getName() + " was affected by your social request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				final int abnormal = Integer.decode("0x" + st.nextToken());
				
				if (st.hasMoreTokens())
				{
					final String targetOrRadius = st.nextToken();
					if (targetOrRadius != null)
					{
						Player player = World.getInstance().getPlayer(targetOrRadius);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
								activeChar.sendMessage(player.getName() + " was affected by your abnormal request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							final int radius = Integer.parseInt(targetOrRadius);
							
							for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
								performAbnormal(abnormal, object);
							
							activeChar.sendMessage(radius + " units radius was affected by your abnormal request.");
						}
					}
				}
				else
				{
					WorldObject obj = activeChar.getTarget();
					if (obj == null)
						obj = activeChar;
					
					if (performAbnormal(abnormal, obj))
						activeChar.sendMessage(obj.getName() + " was affected by your abnormal request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				WorldObject obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				
				if (st.hasMoreTokens())
					level = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					hittime = Integer.parseInt(st.nextToken());
				
				if (obj == null)
					obj = activeChar;
				
				if (!(obj instanceof Creature))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
				{
					Creature target = (Creature) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		
		if (command.contains("menu"))
		{
			String filename = "effects_menu.htm";
			if (command.contains("abnormal"))
				filename = "abnormal.htm";
			else if (command.contains("social"))
				filename = "social.htm";
			
			AdminHelpPage.showHelpPage(activeChar, filename);
		}
		
		return true;
	}
	
	private static boolean performAbnormal(int action, WorldObject target)
	{
		if (target instanceof Creature)
		{
			final Creature character = (Creature) target;
			if ((character.getAbnormalEffect() & action) == action)
				character.stopAbnormalEffect(action);
			else
				character.startAbnormalEffect(action);
			
			return true;
		}
		return false;
	}
	
	private static boolean performSocial(int action, WorldObject target)
	{
		if (target instanceof Creature)
		{
			if (target instanceof Summon || target instanceof Chest || (target instanceof Npc && (action < 1 || action > 3)) || (target instanceof Player && (action < 2 || action > 16)))
				return false;
			
			final Creature character = (Creature) target;
			character.broadcastPacket(new SocialAction(character, action));
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}