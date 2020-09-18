package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToPawn;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.FourSepulchersManager;
import dev.l2j.tesla.gameserver.data.xml.DoorData;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class SepulcherNpc extends Folk
{
	private static final String HTML_FILE_PATH = "data/html/sepulchers/";
	private static final int HALLS_KEY = 7260;
	
	public SepulcherNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
				player.getAI().setIntention(IntentionType.ATTACK, this);
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the Player and this instance.
				if (!canInteract(player))
					player.getAI().setIntention(IntentionType.INTERACT, this);
				else
				{
					// Stop moving if we're already in interact range.
					if (player.isMoving() || player.isInCombat())
						player.getAI().setIntention(IntentionType.IDLE);
					
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
					if (hasRandomAnimation())
						onRandomAnimation(Rnd.get(8));
					
					doAction(player);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		// Check if the Player is a GM ; send him NPC infos if true.
		if (player.isGM())
			sendNpcInfos(player);
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player))
			{
				if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().setIntention(IntentionType.ATTACK, this);
				else
					player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (canInteract(player))
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));
				
				doAction(player);
			}
			else
				player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private void doAction(Player player)
	{
		if (isDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (getNpcId())
		{
			case 31468:
			case 31469:
			case 31470:
			case 31471:
			case 31472:
			case 31473:
			case 31474:
			case 31475:
			case 31476:
			case 31477:
			case 31478:
			case 31479:
			case 31480:
			case 31481:
			case 31482:
			case 31483:
			case 31484:
			case 31485:
			case 31486:
			case 31487:
				// Time limit is reached. You can't open anymore Mysterious boxes after the 49th minute.
				if (Calendar.getInstance().get(Calendar.MINUTE) >= 50)
				{
					broadcastNpcSay("You can start at the scheduled time.");
					return;
				}
				FourSepulchersManager.getInstance().spawnMonster(getNpcId());
				deleteMe();
				break;
			
			case 31455:
			case 31456:
			case 31457:
			case 31458:
			case 31459:
			case 31460:
			case 31461:
			case 31462:
			case 31463:
			case 31464:
			case 31465:
			case 31466:
			case 31467:
				if (player.isInParty() && !player.getParty().isLeader(player))
					player = player.getParty().getLeader();
				
				player.addItem("Quest", HALLS_KEY, 1, player, true);
				
				deleteMe();
				break;
			
			default:
			{
				List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.QUEST_START);
				if (scripts != null && !scripts.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				scripts = getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
				if (scripts != null && scripts.size() == 1)
					scripts.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return HTML_FILE_PATH + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("open_gate"))
		{
			final ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if (hallsKey == null)
				showHtmlFile(player, "Gatekeeper-no.htm");
			else if (FourSepulchersManager.getInstance().isAttackTime())
			{
				switch (getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						FourSepulchersManager.getInstance().spawnShadow(getNpcId());
					default:
					{
						openNextDoor(getNpcId());
						
						final Party party = player.getParty();
						if (party != null)
						{
							for (Player member : player.getParty().getMembers())
							{
								final ItemInstance key = member.getInventory().getItemByItemId(HALLS_KEY);
								if (key != null)
									member.destroyItemByItemId("Quest", HALLS_KEY, key.getCount(), member, true);
							}
						}
						else
							player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.getCount(), player, true);
					}
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public void openNextDoor(int npcId)
	{
		final int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId);
		final Door door = DoorData.getInstance().getDoor(doorId);
		
		// Open the door.
		door.openMe();
		
		// Schedule the automatic door close.
		ThreadPool.schedule(() -> door.closeMe(), 10000);
		
		// Spawn the next mysterious box.
		FourSepulchersManager.getInstance().spawnMysteriousBox(npcId);
		
		sayInShout("The monsters have spawned!");
	}
	
	public void sayInShout(String msg)
	{
		if (msg == null || msg.isEmpty())
			return;
		
		final CreatureSay sm = new CreatureSay(getObjectId(), Say2.SHOUT, getName(), msg);
		for (Player player : getKnownType(Player.class))
			player.sendPacket(sm);
	}
	
	public void showHtmlFile(Player player, String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/sepulchers/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}