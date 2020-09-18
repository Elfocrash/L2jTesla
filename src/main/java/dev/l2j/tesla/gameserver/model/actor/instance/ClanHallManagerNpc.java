package dev.l2j.tesla.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.clanhall.ClanHall;
import dev.l2j.tesla.gameserver.model.clanhall.ClanHallFunction;
import dev.l2j.tesla.gameserver.model.location.TeleportLocation;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.ClanHallManager;
import dev.l2j.tesla.gameserver.data.xml.TeleportLocationData;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.ClanHallDecoration;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.WarehouseDepositList;
import dev.l2j.tesla.gameserver.network.serverpackets.WarehouseWithdrawList;

public class ClanHallManagerNpc extends Merchant
{
	protected static final int COND_OWNER_FALSE = 0;
	protected static final int COND_ALL_FALSE = 1;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
	protected static final int COND_OWNER = 3;
	
	private static final String hp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]";
	private static final String hp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]";
	private static final String hp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]";
	private static final String hp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
	private static final String exp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]";
	private static final String exp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]";
	private static final String exp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]";
	private static final String exp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
	private static final String mp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
	private static final String mp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
	private static final String mp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]";
	private static final String mp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
	
	private static final String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
	private static final String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]";
	private static final String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
	private static final String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]";
	private static final String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]";
	private static final String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]";
	
	private static final String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]";
	private static final String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]";
	
	private ClanHall _clanHall;
	
	public ClanHallManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		_clanHall = ClanHallManager.getInstance().getNearestClanHall(getX(), getY(), 500);
		if (_clanHall == null)
			LOGGER.warn("Couldn't find the nearest ClanHall for {}.", toString());
		
		super.onSpawn();
	}
	
	@Override
	public boolean isWarehouse()
	{
		return true;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		
		if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken();
			
			String val = (st.hasMoreTokens()) ? st.nextToken() : "";
			
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				
				if ((player.getClanPrivileges() & Clan.CP_CH_DISMISS) == Clan.CP_CH_DISMISS)
				{
					if (val.equalsIgnoreCase("list"))
						html.setFile("data/html/clanHallManager/banish-list.htm");
					else if (val.equalsIgnoreCase("banish"))
					{
						_clanHall.banishForeigners();
						html.setFile("data/html/clanHallManager/banish.htm");
					}
				}
				else
					html.setFile("data/html/clanHallManager/not_authorized.htm");
				
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				
				if ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) == Clan.CP_CL_VIEW_WAREHOUSE)
				{
					html.setFile("data/html/clanHallManager/vault.htm");
					html.replace("%rent%", _clanHall.getLease());
					html.replace("%date%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(_clanHall.getPaidUntil()));
				}
				else
					html.setFile("data/html/clanHallManager/not_authorized.htm");
				
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("door"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				
				if ((player.getClanPrivileges() & Clan.CP_CH_OPEN_DOOR) == Clan.CP_CH_OPEN_DOOR)
				{
					if (val.equalsIgnoreCase("open"))
					{
						_clanHall.openCloseDoors(true);
						html.setFile("data/html/clanHallManager/door-open.htm");
					}
					else if (val.equalsIgnoreCase("close"))
					{
						_clanHall.openCloseDoors(false);
						html.setFile("data/html/clanHallManager/door-close.htm");
					}
					else
						html.setFile("data/html/clanHallManager/door.htm");
				}
				else
					html.setFile("data/html/clanHallManager/not_authorized.htm");
				
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_TELEPORT);
					if (chf == null)
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					else
						html.setFile("data/html/clanHallManager/tele" + _clanHall.getLocation() + chf.getLvl() + ".htm");
					
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
				else if (val.equalsIgnoreCase("item_creation"))
				{
					if (!st.hasMoreTokens())
						return;
					
					final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
					if (chf == null)
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
						return;
					}
					
					showBuyWindow(player, Integer.parseInt(st.nextToken()) + (chf.getLvl() * 100000));
				}
				else if (val.equalsIgnoreCase("support"))
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_SUPPORT);
					if (chf == null)
						html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
					else
					{
						html.setFile("data/html/clanHallManager/support" + chf.getLvl() + ".htm");
						html.replace("%mp%", (int) getCurrentMp());
					}
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
				else if (val.equalsIgnoreCase("back"))
					showChatWindow(player);
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/clanHallManager/functions.htm");
					
					final ClanHallFunction chfExp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
					if (chfExp != null)
						html.replace("%xp_regen%", chfExp.getLvl());
					else
						html.replace("%xp_regen%", "0");
					
					final ClanHallFunction chfHp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
					if (chfHp != null)
						html.replace("%hp_regen%", chfHp.getLvl());
					else
						html.replace("%hp_regen%", "0");
					
					final ClanHallFunction chfMp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
					if (chfMp != null)
						html.replace("%mp_regen%", chfMp.getLvl());
					else
						html.replace("%mp_regen%", "0");
					
					html.replace("%npcId%", getNpcId());
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CH_SET_FUNCTIONS) == Clan.CP_CH_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.hasMoreTokens())
						{
							if (_clanHall.getOwnerId() == 0)
								return;
							
							val = st.nextToken();
							
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Fireplace (HP Recovery Device)");
								
								final int percent = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (percent)
								{
									case 20:
										cost = Config.CH_HPREG1_FEE;
										break;
									
									case 40:
										cost = Config.CH_HPREG2_FEE;
										break;
									
									case 80:
										cost = Config.CH_HPREG3_FEE;
										break;
									
									case 100:
										cost = Config.CH_HPREG4_FEE;
										break;
									
									case 120:
										cost = Config.CH_HPREG5_FEE;
										break;
									
									case 140:
										cost = Config.CH_HPREG6_FEE;
										break;
									
									case 160:
										cost = Config.CH_HPREG7_FEE;
										break;
									
									case 180:
										cost = Config.CH_HPREG8_FEE;
										break;
									
									case 200:
										cost = Config.CH_HPREG9_FEE;
										break;
									
									case 220:
										cost = Config.CH_HPREG10_FEE;
										break;
									
									case 240:
										cost = Config.CH_HPREG11_FEE;
										break;
									
									case 260:
										cost = Config.CH_HPREG12_FEE;
										break;
									
									default:
										cost = Config.CH_HPREG13_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_HPREG_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Provides additional HP recovery for clan members in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
								html.replace("%apply%", "recovery hp " + percent);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Carpet (MP Recovery)");
								
								final int percent = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_MPREG1_FEE;
										break;
									
									case 10:
										cost = Config.CH_MPREG2_FEE;
										break;
									
									case 15:
										cost = Config.CH_MPREG3_FEE;
										break;
									
									case 30:
										cost = Config.CH_MPREG4_FEE;
										break;
									
									default:
										cost = Config.CH_MPREG5_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_MPREG_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Provides additional MP recovery for clan members in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
								html.replace("%apply%", "recovery mp " + percent);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Chandelier (EXP Recovery Device)");
								
								final int percent = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CH_EXPREG1_FEE;
										break;
									
									case 10:
										cost = Config.CH_EXPREG2_FEE;
										break;
									
									case 15:
										cost = Config.CH_EXPREG3_FEE;
										break;
									
									case 25:
										cost = Config.CH_EXPREG4_FEE;
										break;
									
									case 35:
										cost = Config.CH_EXPREG5_FEE;
										break;
									
									case 40:
										cost = Config.CH_EXPREG6_FEE;
										break;
									
									default:
										cost = Config.CH_EXPREG7_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_EXPREG_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Restores the Exp of any clan member who is resurrected in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
								html.replace("%apply%", "recovery exp " + percent);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								val = st.nextToken();
								final int percent = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
								if (chf != null && chf.getLvl() == percent)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", val + "%");
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 20:
										fee = Config.CH_HPREG1_FEE;
										break;
									
									case 40:
										fee = Config.CH_HPREG2_FEE;
										break;
									
									case 80:
										fee = Config.CH_HPREG3_FEE;
										break;
									
									case 100:
										fee = Config.CH_HPREG4_FEE;
										break;
									
									case 120:
										fee = Config.CH_HPREG5_FEE;
										break;
									
									case 140:
										fee = Config.CH_HPREG6_FEE;
										break;
									
									case 160:
										fee = Config.CH_HPREG7_FEE;
										break;
									
									case 180:
										fee = Config.CH_HPREG8_FEE;
										break;
									
									case 200:
										fee = Config.CH_HPREG9_FEE;
										break;
									
									case 220:
										fee = Config.CH_HPREG10_FEE;
										break;
									
									case 240:
										fee = Config.CH_HPREG11_FEE;
										break;
									
									case 260:
										fee = Config.CH_HPREG12_FEE;
										break;
									
									default:
										fee = Config.CH_HPREG13_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_HP, percent, fee, Config.CH_HPREG_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								val = st.nextToken();
								final int percent = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
								if (chf != null && chf.getLvl() == percent)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", val + "%");
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 5:
										fee = Config.CH_MPREG1_FEE;
										break;
									
									case 10:
										fee = Config.CH_MPREG2_FEE;
										break;
									
									case 15:
										fee = Config.CH_MPREG3_FEE;
										break;
									
									case 30:
										fee = Config.CH_MPREG4_FEE;
										break;
									
									default:
										fee = Config.CH_MPREG5_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_MP, percent, fee, Config.CH_MPREG_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								val = st.nextToken();
								final int percent = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
								if (chf != null && chf.getLvl() == percent)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", val + "%");
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 5:
										fee = Config.CH_EXPREG1_FEE;
										break;
									
									case 10:
										fee = Config.CH_EXPREG2_FEE;
										break;
									
									case 15:
										fee = Config.CH_EXPREG3_FEE;
										break;
									
									case 25:
										fee = Config.CH_EXPREG4_FEE;
										break;
									
									case 35:
										fee = Config.CH_EXPREG5_FEE;
										break;
									
									case 40:
										fee = Config.CH_EXPREG6_FEE;
										break;
									
									default:
										fee = Config.CH_EXPREG7_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_RESTORE_EXP, percent, fee, Config.CH_EXPREG_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
						}
						else
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setFile("data/html/clanHallManager/edit_recovery.htm");
							
							final int grade = _clanHall.getGrade();
							
							// Restore HP function.
							final ClanHallFunction chfHp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
							if (chfHp != null)
							{
								html.replace("%hp_recovery%", chfHp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfHp.getLease() + "</font> adenas / " + (Config.CH_HPREG_FEE_RATIO / 86400000) + " day)");
								html.replace("%hp_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfHp.getEndTime()));
								
								switch (grade)
								{
									case 0:
										html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]" + hp_grade0);
										break;
									
									case 1:
										html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]" + hp_grade1);
										break;
									
									case 2:
										html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]" + hp_grade2);
										break;
									
									case 3:
										html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>]" + hp_grade3);
										break;
								}
							}
							else
							{
								html.replace("%hp_recovery%", "none");
								html.replace("%hp_period%", "none");
								
								switch (grade)
								{
									case 0:
										html.replace("%change_hp%", hp_grade0);
										break;
									
									case 1:
										html.replace("%change_hp%", hp_grade1);
										break;
									
									case 2:
										html.replace("%change_hp%", hp_grade2);
										break;
									
									case 3:
										html.replace("%change_hp%", hp_grade3);
										break;
								}
							}
							
							// Restore exp function.
							final ClanHallFunction chfExp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
							if (chfExp != null)
							{
								html.replace("%exp_recovery%", chfExp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfExp.getLease() + "</font> adenas / " + (Config.CH_EXPREG_FEE_RATIO / 86400000) + " day)");
								html.replace("%exp_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfExp.getEndTime()));
								
								switch (grade)
								{
									case 0:
										html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]" + exp_grade0);
										break;
									
									case 1:
										html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]" + exp_grade1);
										break;
									
									case 2:
										html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]" + exp_grade2);
										break;
									
									case 3:
										html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>]" + exp_grade3);
										break;
								}
							}
							else
							{
								html.replace("%exp_recovery%", "none");
								html.replace("%exp_period%", "none");
								
								switch (grade)
								{
									case 0:
										html.replace("%change_exp%", exp_grade0);
										break;
									
									case 1:
										html.replace("%change_exp%", exp_grade1);
										break;
									
									case 2:
										html.replace("%change_exp%", exp_grade2);
										break;
									
									case 3:
										html.replace("%change_exp%", exp_grade3);
										break;
								}
							}
							
							// Restore MP function.
							final ClanHallFunction chfMp = _clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
							if (chfMp != null)
							{
								html.replace("%mp_recovery%", chfMp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfMp.getLease() + "</font> adenas / " + (Config.CH_MPREG_FEE_RATIO / 86400000) + " day)");
								html.replace("%mp_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfMp.getEndTime()));
								
								switch (grade)
								{
									case 0:
										html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]" + mp_grade0);
										break;
									
									case 1:
										html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]" + mp_grade1);
										break;
									
									case 2:
										html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]" + mp_grade2);
										break;
									
									case 3:
										html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>]" + mp_grade3);
										break;
								}
							}
							else
							{
								html.replace("%mp_recovery%", "none");
								html.replace("%mp_period%", "none");
								
								switch (grade)
								{
									case 0:
										html.replace("%change_mp%", mp_grade0);
										break;
									
									case 1:
										html.replace("%change_mp%", mp_grade1);
										break;
									
									case 2:
										html.replace("%change_mp%", mp_grade2);
										break;
									
									case 3:
										html.replace("%change_mp%", mp_grade3);
										break;
								}
							}
							html.replace("%objectId%", getObjectId());
							player.sendPacket(html);
						}
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.hasMoreTokens())
						{
							if (_clanHall.getOwnerId() == 0)
								return;
							
							val = st.nextToken();
							
							if (val.equalsIgnoreCase("item_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other item 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("tele_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_item"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Magic Equipment (Item Production Facilities)");
								
								final int stage = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_ITEM1_FEE;
										break;
									
									case 2:
										cost = Config.CH_ITEM2_FEE;
										break;
									
									default:
										cost = Config.CH_ITEM3_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_ITEM_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Allow the purchase of special items at fixed intervals.");
								html.replace("%apply%", "other item " + stage);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Insignia (Supplementary Magic)");
								
								final int stage = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_SUPPORT1_FEE;
										break;
									
									case 2:
										cost = Config.CH_SUPPORT2_FEE;
										break;
									
									case 3:
										cost = Config.CH_SUPPORT3_FEE;
										break;
									
									case 4:
										cost = Config.CH_SUPPORT4_FEE;
										break;
									
									case 5:
										cost = Config.CH_SUPPORT5_FEE;
										break;
									
									case 6:
										cost = Config.CH_SUPPORT6_FEE;
										break;
									
									case 7:
										cost = Config.CH_SUPPORT7_FEE;
										break;
									
									default:
										cost = Config.CH_SUPPORT8_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_SUPPORT_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Enables the use of supplementary magic.");
								html.replace("%apply%", "other support " + stage);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Mirror (Teleportation Device)");
								
								final int stage = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_TELE1_FEE;
										break;
									
									default:
										cost = Config.CH_TELE2_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_TELE_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Teleports clan members in a clan hall to the target <font color=\"00FFFF\">Stage " + stage + "</font> staging area");
								html.replace("%apply%", "other tele " + stage);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("item"))
							{
								if (_clanHall.getOwnerId() == 0)
									return;
								
								val = st.nextToken();
								final int lvl = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
								if (chf != null && chf.getLvl() == lvl)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", "Stage " + val);
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 1:
										fee = Config.CH_ITEM1_FEE;
										break;
									
									case 2:
										fee = Config.CH_ITEM2_FEE;
										break;
									
									default:
										fee = Config.CH_ITEM3_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_ITEM_CREATE, lvl, fee, Config.CH_ITEM_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								val = st.nextToken();
								final int lvl = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_TELEPORT);
								if (chf != null && chf.getLvl() == lvl)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", "Stage " + val);
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 1:
										fee = Config.CH_TELE1_FEE;
										break;
									
									default:
										fee = Config.CH_TELE2_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_TELEPORT, lvl, fee, Config.CH_TELE_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("support"))
							{
								val = st.nextToken();
								final int lvl = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_SUPPORT);
								if (chf != null && chf.getLvl() == lvl)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", "Stage " + val);
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 1:
										fee = Config.CH_SUPPORT1_FEE;
										break;
									
									case 2:
										fee = Config.CH_SUPPORT2_FEE;
										break;
									
									case 3:
										fee = Config.CH_SUPPORT3_FEE;
										break;
									
									case 4:
										fee = Config.CH_SUPPORT4_FEE;
										break;
									
									case 5:
										fee = Config.CH_SUPPORT5_FEE;
										break;
									
									case 6:
										fee = Config.CH_SUPPORT6_FEE;
										break;
									
									case 7:
										fee = Config.CH_SUPPORT7_FEE;
										break;
									
									default:
										fee = Config.CH_SUPPORT8_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_SUPPORT, lvl, fee, Config.CH_SUPPORT_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
						}
						else
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setFile("data/html/clanHallManager/edit_other.htm");
							
							final ClanHallFunction chfTel = _clanHall.getFunction(ClanHall.FUNC_TELEPORT);
							if (chfTel != null)
							{
								html.replace("%tele%", "- Stage " + chfTel.getLvl() + "</font> (<font color=\"FFAABB\">" + chfTel.getLease() + "</font> adenas / " + (Config.CH_TELE_FEE_RATIO / 86400000) + " day)");
								html.replace("%tele_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfTel.getEndTime()));
								html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Remove</a>]" + tele);
							}
							else
							{
								html.replace("%tele%", "none");
								html.replace("%tele_period%", "none");
								html.replace("%change_tele%", tele);
							}
							
							final int grade = _clanHall.getGrade();
							
							final ClanHallFunction chfSup = _clanHall.getFunction(ClanHall.FUNC_SUPPORT);
							if (chfSup != null)
							{
								html.replace("%support%", "- Stage " + chfSup.getLvl() + "</font> (<font color=\"FFAABB\">" + chfSup.getLease() + "</font> adenas / " + (Config.CH_SUPPORT_FEE_RATIO / 86400000) + " day)");
								html.replace("%support_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfSup.getEndTime()));
								
								switch (grade)
								{
									case 0:
										html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]" + support_grade0);
										break;
									
									case 1:
										html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]" + support_grade1);
										break;
									
									case 2:
										html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]" + support_grade2);
										break;
									
									case 3:
										html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>]" + support_grade3);
										break;
								}
							}
							else
							{
								html.replace("%support%", "none");
								html.replace("%support_period%", "none");
								
								switch (grade)
								{
									case 0:
										html.replace("%change_support%", support_grade0);
										break;
									
									case 1:
										html.replace("%change_support%", support_grade1);
										break;
									
									case 2:
										html.replace("%change_support%", support_grade2);
										break;
									
									case 3:
										html.replace("%change_support%", support_grade3);
										break;
								}
							}
							
							final ClanHallFunction chfCreate = _clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
							if (chfCreate != null)
							{
								html.replace("%item%", "- Stage " + chfCreate.getLvl() + "</font> (<font color=\"FFAABB\">" + chfCreate.getLease() + "</font> adenas / " + (Config.CH_ITEM_FEE_RATIO / 86400000) + " day)");
								html.replace("%item_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfCreate.getEndTime()));
								html.replace("%change_item%", "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Remove</a>]" + item);
							}
							else
							{
								html.replace("%item%", "none");
								html.replace("%item_period%", "none");
								html.replace("%change_item%", item);
							}
							html.replace("%objectId%", getObjectId());
							player.sendPacket(html);
						}
					}
					else if (val.equalsIgnoreCase("deco"))
					{
						if (st.hasMoreTokens())
						{
							if (_clanHall.getOwnerId() == 0)
								return;
							
							val = st.nextToken();
							if (val.equalsIgnoreCase("curtains_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco curtains 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("fixtures_cancel"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-cancel.htm");
								html.replace("%apply%", "deco fixtures 0");
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_curtains"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Curtains (Decoration)");
								
								final int stage = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_CURTAIN1_FEE;
										break;
									
									default:
										cost = Config.CH_CURTAIN2_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_CURTAIN_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "These curtains can be used to decorate the clan hall.");
								html.replace("%apply%", "deco curtains " + stage);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("edit_fixtures"))
							{
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								html.setFile("data/html/clanHallManager/functions-apply.htm");
								html.replace("%name%", "Front Platform (Decoration)");
								
								final int stage = Integer.parseInt(st.nextToken());
								
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CH_FRONT1_FEE;
										break;
									
									default:
										cost = Config.CH_FRONT2_FEE;
										break;
								}
								
								html.replace("%cost%", cost + "</font> adenas / " + (Config.CH_FRONT_FEE_RATIO / 86400000) + " day</font>)");
								html.replace("%use%", "Used to decorate the clan hall.");
								html.replace("%apply%", "deco fixtures " + stage);
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("curtains"))
							{
								val = st.nextToken();
								final int lvl = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
								if (chf != null && chf.getLvl() == lvl)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", "Stage " + val);
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 1:
										fee = Config.CH_CURTAIN1_FEE;
										break;
									
									default:
										fee = Config.CH_CURTAIN2_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_DECO_CURTAINS, lvl, fee, Config.CH_CURTAIN_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
							else if (val.equalsIgnoreCase("fixtures"))
							{
								val = st.nextToken();
								final int lvl = Integer.parseInt(val);
								
								final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
								
								final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
								if (chf != null && chf.getLvl() == lvl)
								{
									html.setFile("data/html/clanHallManager/functions-used.htm");
									html.replace("%val%", "Stage " + val);
									html.replace("%objectId%", getObjectId());
									player.sendPacket(html);
									return;
								}
								
								html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
								
								int fee;
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
										break;
									
									case 1:
										fee = Config.CH_FRONT1_FEE;
										break;
									
									default:
										fee = Config.CH_FRONT2_FEE;
										break;
								}
								
								if (!_clanHall.updateFunctions(player, ClanHall.FUNC_DECO_FRONTPLATEFORM, lvl, fee, Config.CH_FRONT_FEE_RATIO))
									html.setFile("data/html/clanHallManager/low_adena.htm");
								else
									revalidateDeco(player);
								
								html.replace("%objectId%", getObjectId());
								player.sendPacket(html);
							}
						}
						else
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setFile("data/html/clanHallManager/deco.htm");
							
							final ClanHallFunction chfCurtains = _clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
							if (chfCurtains != null)
							{
								html.replace("%curtain%", "- Stage " + chfCurtains.getLvl() + "</font> (<font color=\"FFAABB\">" + chfCurtains.getLease() + "</font> adenas / " + (Config.CH_CURTAIN_FEE_RATIO / 86400000) + " day)");
								html.replace("%curtain_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfCurtains.getEndTime()));
								html.replace("%change_curtain%", "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Remove</a>]" + curtains);
							}
							else
							{
								html.replace("%curtain%", "none");
								html.replace("%curtain_period%", "none");
								html.replace("%change_curtain%", curtains);
							}
							
							final ClanHallFunction chfPlateform = _clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
							if (chfPlateform != null)
							{
								html.replace("%fixture%", "- Stage " + chfPlateform.getLvl() + "</font> (<font color=\"FFAABB\">" + chfPlateform.getLease() + "</font> adenas / " + (Config.CH_FRONT_FEE_RATIO / 86400000) + " day)");
								html.replace("%fixture_period%", "Next fee at " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(chfPlateform.getEndTime()));
								html.replace("%change_fixture%", "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Remove</a>]" + fixtures);
							}
							else
							{
								html.replace("%fixture%", "none");
								html.replace("%fixture_period%", "none");
								html.replace("%change_fixture%", fixtures);
							}
							html.replace("%objectId%", getObjectId());
							player.sendPacket(html);
						}
					}
					else if (val.equalsIgnoreCase("back"))
						showChatWindow(player);
					else
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/clanHallManager/manage.htm");
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
					}
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/clanHallManager/not_authorized.htm");
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_SUPPORT);
				if (chf == null || chf.getLvl() == 0)
					return;
				
				if (player.isCursedWeaponEquipped())
				{
					// Custom system message
					player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs");
					return;
				}
				
				setTarget(player);
				
				try
				{
					final int id = Integer.parseInt(val);
					final int lvl = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 0;
					
					final L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
					if (skill.getSkillType() == L2SkillType.SUMMON)
						player.doSimultaneousCast(skill);
					else
					{
						if (!((skill.getMpConsume() + skill.getMpInitialConsume()) > getCurrentMp()))
							doCast(skill);
						else
						{
							final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setFile("data/html/clanHallManager/support-no_mana.htm");
							html.replace("%mp%", (int) getCurrentMp());
							html.replace("%objectId%", getObjectId());
							player.sendPacket(html);
							return;
						}
					}
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/clanHallManager/support-done.htm");
					html.replace("%mp%", (int) getCurrentMp());
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid skill, contact your server support.");
				}
			}
			else if (actualCommand.equalsIgnoreCase("list_back"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/clanHallManager/chamberlain.htm");
				html.replace("%npcname%", getName());
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				final ClanHallFunction chf = _clanHall.getFunction(ClanHall.FUNC_SUPPORT);
				if (chf == null || chf.getLvl() == 0)
					return;
				
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/clanHallManager/support" + _clanHall.getFunction(ClanHall.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", (int) getStatus().getCurrentMp());
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				final TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(val));
				if (list != null && player.reduceAdena("Teleport", list.getPrice(), this, true))
					player.teleportTo(list, 0);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (actualCommand.equalsIgnoreCase("WithdrawC"))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) != Clan.CP_CL_VIEW_WAREHOUSE)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
					return;
				}
				
				if (player.getClan().getLevel() == 0)
					player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				else
				{
					player.setActiveWarehouse(player.getClan().getWarehouse());
					player.sendPacket(new WarehouseWithdrawList(player, WarehouseWithdrawList.CLAN));
				}
			}
			else if (actualCommand.equalsIgnoreCase("DepositC"))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if (player.getClan() != null)
				{
					if (player.getClan().getLevel() == 0)
						player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
					else
					{
						player.setActiveWarehouse(player.getClan().getWarehouse());
						player.tempInventoryDisable();
						player.sendPacket(new WarehouseDepositList(player, WarehouseDepositList.CLAN));
					}
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/clanHallManager/chamberlain-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == COND_OWNER)
			filename = "data/html/clanHallManager/chamberlain.htm";
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	protected int validateCondition(Player player)
	{
		if (_clanHall == null)
			return COND_ALL_FALSE;
		
		if (player.getClan() != null)
		{
			if (_clanHall.getOwnerId() == player.getClanId())
				return COND_OWNER;
			
			return COND_OWNER_FALSE;
		}
		return COND_ALL_FALSE;
	}
	
	private void revalidateDeco(Player player)
	{
		_clanHall.getZone().broadcastPacket(new ClanHallDecoration(_clanHall));
	}
}