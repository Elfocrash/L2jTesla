package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.Map.Entry;
import java.util.StringTokenizer;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcInventory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.enums.CabalType;
import dev.l2j.tesla.gameserver.enums.SealType;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class SignsPriest extends Folk
{
	public SignsPriest(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player.getCurrentFolk() == null || player.getCurrentFolk().getObjectId() != getObjectId())
			return;
		
		if (command.startsWith("SevenSignsDesc"))
		{
			showChatWindow(player, Integer.parseInt(command.substring(15)), null, true);
		}
		else if (command.startsWith("SevenSigns"))
		{
			String path;
			
			CabalType cabal = CabalType.NORMAL;
			int stoneType = 0;
			
			final long ancientAdenaAmount = player.getAncientAdena();
			
			int val = Integer.parseInt(command.substring(11, 12).trim());
			
			if (command.length() > 12) // SevenSigns x[x] x [x..x]
				val = Integer.parseInt(command.substring(11, 13).trim());
			
			if (command.length() > 13)
			{
				try
				{
					cabal = CabalType.VALUES[Integer.parseInt(command.substring(14, 15).trim())];
				}
				catch (Exception e)
				{
					try
					{
						cabal = CabalType.VALUES[Integer.parseInt(command.substring(13, 14).trim())];
					}
					catch (Exception e2)
					{
						try
						{
							StringTokenizer st = new StringTokenizer(command.trim());
							st.nextToken();
							cabal = CabalType.VALUES[Integer.parseInt(st.nextToken())];
						}
						catch (Exception e3)
						{
							LOGGER.warn("Failed to retrieve cabal from bypass command. NpcId: {}, command: {}.", getNpcId(), command);
						}
					}
				}
			}
			
			switch (val)
			{
				case 2: // Purchase Record of the Seven Signs
					if (!player.getInventory().validateCapacity(1))
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						break;
					}
					
					if (!player.reduceAdena("SevenSigns", SevenSignsManager.RECORD_SEVEN_SIGNS_COST, this, true))
						break;
					
					player.addItem("SevenSigns", SevenSignsManager.RECORD_SEVEN_SIGNS_ID, 1, player, true);
					
					if (this instanceof DawnPriest)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				
				case 33: // "I want to participate" request
					CabalType oldCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
					
					if (oldCabal != CabalType.NORMAL)
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, val, "dawn_member", false);
						else
							showChatWindow(player, val, "dusk_member", false);
						return;
					}
					else if (player.getClassId().level() == 0)
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, val, "dawn_firstclass", false);
						else
							showChatWindow(player, val, "dusk_firstclass", false);
						return;
					}
					else if (cabal == CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK) // dusk
					{
						// castle owners cannot participate with dusk side
						if (player.getClan() != null && player.getClan().hasCastle())
						{
							showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
							break;
						}
					}
					else if (cabal == CabalType.DAWN && Config.ALT_GAME_CASTLE_DAWN) // dawn
					{
						// clans without castle need to pay participation fee
						if (player.getClan() == null || !player.getClan().hasCastle())
						{
							showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm");
							break;
						}
					}
					
					if (this instanceof DawnPriest)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				
				case 34: // Pay the participation fee request
					ItemInstance adena = player.getInventory().getItemByItemId(PcInventory.ADENA_ID); // adena
					ItemInstance certif = player.getInventory().getItemByItemId(6388); // Lord of the Manor's Certificate of Approval
					boolean fee = true;
					
					if (player.getClassId().level() < 2 || (adena != null && adena.getCount() >= SevenSignsManager.ADENA_JOIN_DAWN_COST) || (certif != null && certif.getCount() >= 1))
						fee = false;
					
					if (fee)
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_no.htm");
					else
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn.htm");
					break;
				
				case 3: // Join Cabal Intro 1
				case 8: // Festival of Darkness Intro - SevenSigns x [0]1
					showChatWindow(player, val, cabal.getShortName(), false);
					break;
				
				case 4: // Join a Cabal - SevenSigns 4 [0]1 x
					SealType newSeal = SealType.VALUES[Integer.parseInt(command.substring(15))];
					
					if (player.getClassId().level() >= 2)
					{
						if (cabal == CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK)
						{
							if (player.getClan() != null && player.getClan().hasCastle()) // even if in htmls is said that ally can have castle too, but its not
							{
								showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm");
								return;
							}
						}
						/*
						 * If the player is trying to join the Lords of Dawn, check if they are carrying a Lord's certificate. If not then try to take the required amount of adena instead.
						 */
						if (Config.ALT_GAME_CASTLE_DAWN && cabal == CabalType.DAWN)
						{
							boolean allowJoinDawn = false;
							
							if (player.getClan() != null && player.getClan().hasCastle()) // castle owner don't need to pay anything
								allowJoinDawn = true;
							else if (player.destroyItemByItemId("SevenSigns", SevenSignsManager.CERTIFICATE_OF_APPROVAL_ID, 1, this, true))
								allowJoinDawn = true;
							else if (player.reduceAdena("SevenSigns", SevenSignsManager.ADENA_JOIN_DAWN_COST, this, true))
								allowJoinDawn = true;
							
							if (!allowJoinDawn)
							{
								showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm");
								return;
							}
						}
					}
					SevenSignsManager.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
					
					if (cabal == CabalType.DAWN)
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN); // Joined Dawn
					else
						player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK); // Joined Dusk
						
					// Show a confirmation message to the user, indicating which seal they chose.
					switch (newSeal)
					{
						case AVARICE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE);
							break;
						
						case GNOSIS:
							player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS);
							break;
						
						case STRIFE:
							player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE);
							break;
					}
					
					showChatWindow(player, 4, cabal.getShortName(), false);
					break;
				
				case 5:
					if (this instanceof DawnPriest)
					{
						if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.NORMAL)
							showChatWindow(player, val, "dawn_no", false);
						else
							showChatWindow(player, val, "dawn", false);
					}
					else
					{
						if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.NORMAL)
							showChatWindow(player, val, "dusk_no", false);
						else
							showChatWindow(player, val, "dusk", false);
					}
					break;
				
				case 21:
					int contribStoneId = Integer.parseInt(command.substring(14, 18));
					
					ItemInstance contribBlueStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_BLUE_ID);
					ItemInstance contribGreenStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_GREEN_ID);
					ItemInstance contribRedStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_RED_ID);
					
					int contribBlueStoneCount = contribBlueStones == null ? 0 : contribBlueStones.getCount();
					int contribGreenStoneCount = contribGreenStones == null ? 0 : contribGreenStones.getCount();
					int contribRedStoneCount = contribRedStones == null ? 0 : contribRedStones.getCount();
					
					int score = SevenSignsManager.getInstance().getPlayerContribScore(player.getObjectId());
					int contributionCount = 0;
					
					boolean contribStonesFound = false;
					
					int redContrib = 0;
					int greenContrib = 0;
					int blueContrib = 0;
					
					try
					{
						contributionCount = Integer.parseInt(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, 6, "dawn_failure", false);
						else
							showChatWindow(player, 6, "dusk_failure", false);
						break;
					}
					
					switch (contribStoneId)
					{
						case SevenSignsManager.SEAL_STONE_BLUE_ID:
							blueContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSignsManager.SEAL_STONE_BLUE_VALUE;
							if (blueContrib > contribBlueStoneCount)
								blueContrib = contributionCount;
							break;
						
						case SevenSignsManager.SEAL_STONE_GREEN_ID:
							greenContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSignsManager.SEAL_STONE_GREEN_VALUE;
							if (greenContrib > contribGreenStoneCount)
								greenContrib = contributionCount;
							break;
						
						case SevenSignsManager.SEAL_STONE_RED_ID:
							redContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSignsManager.SEAL_STONE_RED_VALUE;
							if (redContrib > contribRedStoneCount)
								redContrib = contributionCount;
							break;
					}
					
					if (redContrib > 0)
						contribStonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_RED_ID, redContrib, this, true);
					
					if (greenContrib > 0)
						contribStonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_GREEN_ID, greenContrib, this, true);
					
					if (blueContrib > 0)
						contribStonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_BLUE_ID, blueContrib, this, true);
					
					if (!contribStonesFound)
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, 6, "dawn_low_stones", false);
						else
							showChatWindow(player, 6, "dusk_low_stones", false);
					}
					else
					{
						score = SevenSignsManager.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContrib, greenContrib, redContrib);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(score));
						
						if (this instanceof DawnPriest)
							showChatWindow(player, 6, "dawn", false);
						else
							showChatWindow(player, 6, "dusk", false);
					}
					break;
				
				case 6: // Contribute Seal Stones - SevenSigns 6 x
					stoneType = Integer.parseInt(command.substring(13));
					
					ItemInstance blueStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_BLUE_ID);
					ItemInstance greenStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_GREEN_ID);
					ItemInstance redStones = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_RED_ID);
					
					int blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
					int greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
					int redStoneCount = redStones == null ? 0 : redStones.getCount();
					
					int contribScore = SevenSignsManager.getInstance().getPlayerContribScore(player.getObjectId());
					boolean stonesFound = false;
					
					if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB)
					{
						player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED);
					}
					else
					{
						int redContribCount = 0;
						int greenContribCount = 0;
						int blueContribCount = 0;
						
						String contribStoneColor = null;
						String stoneColorContr = null;
						
						int stoneCountContr = 0;
						int stoneIdContr = 0;
						
						switch (stoneType)
						{
							case 1:
								contribStoneColor = "Blue";
								stoneColorContr = "blue";
								stoneIdContr = SevenSignsManager.SEAL_STONE_BLUE_ID;
								stoneCountContr = blueStoneCount;
								break;
							
							case 2:
								contribStoneColor = "Green";
								stoneColorContr = "green";
								stoneIdContr = SevenSignsManager.SEAL_STONE_GREEN_ID;
								stoneCountContr = greenStoneCount;
								break;
							
							case 3:
								contribStoneColor = "Red";
								stoneColorContr = "red";
								stoneIdContr = SevenSignsManager.SEAL_STONE_RED_ID;
								stoneCountContr = redStoneCount;
								break;
							
							case 4:
								int tempContribScore = contribScore;
								redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSignsManager.SEAL_STONE_RED_VALUE;
								if (redContribCount > redStoneCount)
									redContribCount = redStoneCount;
								
								tempContribScore += redContribCount * SevenSignsManager.SEAL_STONE_RED_VALUE;
								greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSignsManager.SEAL_STONE_GREEN_VALUE;
								if (greenContribCount > greenStoneCount)
									greenContribCount = greenStoneCount;
								
								tempContribScore += greenContribCount * SevenSignsManager.SEAL_STONE_GREEN_VALUE;
								blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSignsManager.SEAL_STONE_BLUE_VALUE;
								if (blueContribCount > blueStoneCount)
									blueContribCount = blueStoneCount;
								
								if (redContribCount > 0)
									stonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_RED_ID, redContribCount, this, true);
								
								if (greenContribCount > 0)
									stonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_GREEN_ID, greenContribCount, this, true);
								
								if (blueContribCount > 0)
									stonesFound |= player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_BLUE_ID, blueContribCount, this, true);
								
								if (!stonesFound)
								{
									if (this instanceof DawnPriest)
										showChatWindow(player, val, "dawn_no_stones", false);
									else
										showChatWindow(player, val, "dusk_no_stones", false);
								}
								else
								{
									contribScore = SevenSignsManager.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContribCount, greenContribCount, redContribCount);
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(contribScore));
									
									if (this instanceof DawnPriest)
										showChatWindow(player, 6, "dawn", false);
									else
										showChatWindow(player, 6, "dusk", false);
								}
								return;
						}
						
						if (this instanceof DawnPriest)
							path = SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_6_dawn_contribute.htm";
						else
							path = SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_6_dusk_contribute.htm";
						
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile(path);
						html.replace("%contribStoneColor%", contribStoneColor);
						html.replace("%stoneColor%", stoneColorContr);
						html.replace("%stoneCount%", stoneCountContr);
						html.replace("%stoneItemId%", stoneIdContr);
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
					}
					break;
				
				case 7: // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
					int ancientAdenaConvert = 0;
					
					try
					{
						ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
					}
					catch (NumberFormatException e)
					{
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					
					if (ancientAdenaConvert < 1)
					{
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					
					if (ancientAdenaAmount < ancientAdenaConvert)
					{
						showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "blkmrkt_4.htm");
						break;
					}
					
					player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
					player.addAdena("SevenSigns", ancientAdenaConvert, this, true);
					
					showChatWindow(player, SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "blkmrkt_5.htm");
					break;
				
				case 9: // Receive Contribution Rewards
					if (SevenSignsManager.getInstance().isSealValidationPeriod() && SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == SevenSignsManager.getInstance().getCabalHighestScore())
					{
						int ancientAdenaReward = SevenSignsManager.getInstance().getAncientAdenaReward(player.getObjectId());
						
						if (ancientAdenaReward < 3)
						{
							if (this instanceof DawnPriest)
								showChatWindow(player, 9, "dawn_b", false);
							else
								showChatWindow(player, 9, "dusk_b", false);
							break;
						}
						
						player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
						
						if (this instanceof DawnPriest)
							showChatWindow(player, 9, "dawn_a", false);
						else
							showChatWindow(player, 9, "dusk_a", false);
					}
					break;
				
				case 11: // Teleport to Hunting Grounds
					try
					{
						String portInfo = command.substring(14).trim();
						StringTokenizer st = new StringTokenizer(portInfo);
						
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						
						int ancientAdenaCost = Integer.parseInt(st.nextToken());
						
						if (ancientAdenaCost > 0)
						{
							if (!player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true))
								break;
						}
						
						player.teleportTo(x, y, z, 0);
					}
					catch (Exception e)
					{
						LOGGER.error("An error occurred while teleporting a player.", e);
					}
					break;
				
				case 16:
					if (this instanceof DawnPriest)
						showChatWindow(player, val, "dawn", false);
					else
						showChatWindow(player, val, "dusk", false);
					break;
				
				case 17: // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 17 x
					stoneType = Integer.parseInt(command.substring(14));
					
					int stoneId = 0;
					int stoneCount = 0;
					int stoneValue = 0;
					
					String stoneColor = null;
					
					switch (stoneType)
					{
						case 1:
							stoneColor = "blue";
							stoneId = SevenSignsManager.SEAL_STONE_BLUE_ID;
							stoneValue = SevenSignsManager.SEAL_STONE_BLUE_VALUE;
							break;
						
						case 2:
							stoneColor = "green";
							stoneId = SevenSignsManager.SEAL_STONE_GREEN_ID;
							stoneValue = SevenSignsManager.SEAL_STONE_GREEN_VALUE;
							break;
						
						case 3:
							stoneColor = "red";
							stoneId = SevenSignsManager.SEAL_STONE_RED_ID;
							stoneValue = SevenSignsManager.SEAL_STONE_RED_VALUE;
							break;
						
						case 4:
							ItemInstance blueStonesAll = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_BLUE_ID);
							ItemInstance greenStonesAll = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_GREEN_ID);
							ItemInstance redStonesAll = player.getInventory().getItemByItemId(SevenSignsManager.SEAL_STONE_RED_ID);
							
							int blueStoneCountAll = blueStonesAll == null ? 0 : blueStonesAll.getCount();
							int greenStoneCountAll = greenStonesAll == null ? 0 : greenStonesAll.getCount();
							int redStoneCountAll = redStonesAll == null ? 0 : redStonesAll.getCount();
							int ancientAdenaRewardAll = 0;
							
							ancientAdenaRewardAll = SevenSignsManager.calcScore(blueStoneCountAll, greenStoneCountAll, redStoneCountAll);
							
							if (ancientAdenaRewardAll == 0)
							{
								if (this instanceof DawnPriest)
									showChatWindow(player, 18, "dawn_no_stones", false);
								else
									showChatWindow(player, 18, "dusk_no_stones", false);
								return;
							}
							
							if (blueStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_BLUE_ID, blueStoneCountAll, this, true);
							if (greenStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_GREEN_ID, greenStoneCountAll, this, true);
							if (redStoneCountAll > 0)
								player.destroyItemByItemId("SevenSigns", SevenSignsManager.SEAL_STONE_RED_ID, redStoneCountAll, this, true);
							
							player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true);
							
							if (this instanceof DawnPriest)
								showChatWindow(player, 18, "dawn", false);
							else
								showChatWindow(player, 18, "dusk", false);
							return;
					}
					
					ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
					if (stoneInstance != null)
						stoneCount = stoneInstance.getCount();
					
					if (this instanceof DawnPriest)
						path = SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_17_dawn.htm";
					else
						path = SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "signs_17_dusk.htm";
					
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(path);
					html.replace("%stoneColor%", stoneColor);
					html.replace("%stoneValue%", stoneValue);
					html.replace("%stoneCount%", stoneCount);
					html.replace("%stoneItemId%", stoneId);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					break;
				
				case 18: // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
					int convertStoneId = Integer.parseInt(command.substring(14, 18));
					int convertCount = 0;
					
					try
					{
						convertCount = Integer.parseInt(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, 18, "dawn_failed", false);
						else
							showChatWindow(player, 18, "dusk_failed", false);
						break;
					}
					
					ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
					
					if (convertItem != null)
					{
						int ancientAdenaReward = 0;
						int totalCount = convertItem.getCount();
						
						if (convertCount <= totalCount && convertCount > 0)
						{
							switch (convertStoneId)
							{
								case SevenSignsManager.SEAL_STONE_BLUE_ID:
									ancientAdenaReward = SevenSignsManager.calcScore(convertCount, 0, 0);
									break;
								case SevenSignsManager.SEAL_STONE_GREEN_ID:
									ancientAdenaReward = SevenSignsManager.calcScore(0, convertCount, 0);
									break;
								case SevenSignsManager.SEAL_STONE_RED_ID:
									ancientAdenaReward = SevenSignsManager.calcScore(0, 0, convertCount);
									break;
							}
							
							if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true))
							{
								player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
								
								if (this instanceof DawnPriest)
									showChatWindow(player, 18, "dawn", false);
								else
									showChatWindow(player, 18, "dusk", false);
							}
						}
						else
						{
							if (this instanceof DawnPriest)
								showChatWindow(player, 18, "dawn_low_stones", false);
							else
								showChatWindow(player, 18, "dusk_low_stones", false);
							break;
						}
					}
					else
					{
						if (this instanceof DawnPriest)
							showChatWindow(player, 18, "dawn_no_stones", false);
						else
							showChatWindow(player, 18, "dusk_no_stones", false);
						break;
					}
					break;
				
				case 19: // Seal Information (for when joining a cabal)
					SealType chosenSeal = SealType.VALUES[Integer.parseInt(command.substring(16))];
					
					String fileSuffix = chosenSeal.getShortName() + "_" + cabal.getShortName();
					
					showChatWindow(player, val, fileSuffix, false);
					break;
				
				case 20: // Seal Status (for when joining a cabal)
					final StringBuilder sb = new StringBuilder();
					
					if (this instanceof DawnPriest)
						sb.append("<html><body>Priest of Dawn:<br><font color=\"LEVEL\">[ Seal Status ]</font><br>");
					else
						sb.append("<html><body>Dusk Priestess:<br><font color=\"LEVEL\">[ Status of the Seals ]</font><br>");
					
					for (Entry<SealType, CabalType> entry : SevenSignsManager.getInstance().getSealOwners().entrySet())
					{
						final SealType seal = entry.getKey();
						final CabalType sealOwner = entry.getValue();
						
						if (sealOwner != CabalType.NORMAL)
							sb.append("[" + seal.getFullName() + ": " + sealOwner.getFullName() + "]<br>");
						else
							sb.append("[" + seal.getFullName() + ": Nothingness]<br>");
					}
					
					sb.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
					
					html = new NpcHtmlMessage(getObjectId());
					html.setHtml(sb.toString());
					player.sendPacket(html);
					break;
				
				default:
					showChatWindow(player, val, null, false);
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		final int npcId = getTemplate().getNpcId();
		String filename = SevenSignsManager.SEVEN_SIGNS_HTML_PATH;
		
		final CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
		final CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 31092: // Black Marketeer of Mammon
				filename += "blkmrkt_1.htm";
				break;
			
			case 31113: // Merchant of Mammon
				final CabalType sealAvariceOwner = SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE);
				switch (winningCabal)
				{
					case DAWN:
						if (playerCabal != winningCabal || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
					
					case DUSK:
						if (playerCabal != winningCabal || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
					
					default:
						player.sendPacket(SystemMessageId.QUEST_EVENT_PERIOD);
						return;
				}
				filename += "mammmerch_1.htm";
				break;
			
			case 31126: // Blacksmith of Mammon
				final CabalType sealGnosisOwner = SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS);
				switch (winningCabal)
				{
					case DAWN:
						if (playerCabal != winningCabal || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
					
					case DUSK:
						if (playerCabal != winningCabal || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
				}
				filename += "mammblack_1.htm";
				break;
			
			default:
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = (getHtmlPath(npcId, val));
				break;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showChatWindow(Player player, int val, String suffix, boolean isDescription)
	{
		String filename = SevenSignsManager.SEVEN_SIGNS_HTML_PATH;
		
		filename += (isDescription) ? "desc_" + val : "signs_" + val;
		filename += (suffix != null) ? "_" + suffix + ".htm" : ".htm";
		
		showChatWindow(player, filename);
	}
}