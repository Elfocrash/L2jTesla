package dev.l2j.tesla.gameserver.scripting.scripts.village_master;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaInfo;

/**
 * @author fernandopm
 */
public class FirstClassChange extends Quest
{
	// Quest Items Dark Elf
	private static final int GAZE_OF_ABYSS = 1244;
	private static final int IRON_HEART = 1252;
	private static final int JEWEL_OF_DARKNESS = 1261;
	private static final int ORB_OF_ABYSS = 1270;
	
	// Quest Items Orcs
	private static final int MARK_OF_RAIDER = 1592;
	private static final int KHAVATARI_TOTEM = 1615;
	private static final int MASK_OF_MEDIUM = 1631;
	
	// Quest Items Dwarf
	private static final int ARTI_MARKS = 1635;
	private static final int SCAV_MARKS = 1642;
	
	// Quest Items Light Elf
	private static final int ELVEN_KNIGHT_BROOCH = 1204;
	private static final int REORIA_RECOMMENDATION = 1217;
	private static final int ETERNITY_DIAMOND = 1230;
	private static final int LEAF_OF_ORACLE = 1235;
	
	// Quest Items Human
	private static final int MEDALLION_OF_WARRIOR = 1145;
	private static final int SWORD_OF_RITUAL = 1161;
	private static final int BEZIQUES_RECOMMENDATION = 1190;
	private static final int BEAD_OF_SEASON = 1292;
	private static final int MARK_OF_FAITH = 1201;
	
	// Reward Item
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	// Classes
	private static final Map<String, int[]> Classes = new HashMap<>();
	{
		// Dark Elf
		Classes.put("PK", new int[]
		{
			32,
			31,
			2,
			15,
			16,
			17,
			18,
			GAZE_OF_ABYSS,
			33
		});
		Classes.put("AS", new int[]
		{
			35,
			31,
			2,
			19,
			20,
			21,
			22,
			IRON_HEART,
			33
		});
		Classes.put("DW", new int[]
		{
			39,
			38,
			2,
			23,
			24,
			25,
			26,
			JEWEL_OF_DARKNESS,
			33
		});
		Classes.put("SO", new int[]
		{
			42,
			38,
			2,
			27,
			28,
			29,
			30,
			ORB_OF_ABYSS,
			33
		});
		
		// Orc
		Classes.put("OR", new int[]
		{
			45,
			44,
			3,
			9,
			10,
			11,
			12,
			MARK_OF_RAIDER,
			23
		});
		Classes.put("OM", new int[]
		{
			47,
			44,
			3,
			13,
			14,
			15,
			16,
			KHAVATARI_TOTEM,
			23
		});
		Classes.put("OS", new int[]
		{
			50,
			49,
			3,
			17,
			18,
			19,
			20,
			MASK_OF_MEDIUM,
			23
		});
		
		// Dwarf
		Classes.put("SC", new int[]
		{
			54,
			53,
			4,
			5,
			6,
			7,
			8,
			SCAV_MARKS,
			11
		});
		Classes.put("AR", new int[]
		{
			56,
			53,
			4,
			5,
			6,
			7,
			8,
			ARTI_MARKS,
			11
		});
		
		// Light Elf
		Classes.put("EK", new int[]
		{
			19,
			18,
			1,
			18,
			19,
			20,
			21,
			ELVEN_KNIGHT_BROOCH,
			40
		});
		Classes.put("ES", new int[]
		{
			22,
			18,
			1,
			22,
			23,
			24,
			25,
			REORIA_RECOMMENDATION,
			40
		});
		Classes.put("EW", new int[]
		{
			26,
			25,
			1,
			15,
			16,
			17,
			18,
			ETERNITY_DIAMOND,
			33
		});
		Classes.put("EO", new int[]
		{
			29,
			25,
			1,
			19,
			20,
			21,
			22,
			LEAF_OF_ORACLE,
			33
		});
		
		// Human
		Classes.put("HW", new int[]
		{
			1,
			0,
			0,
			26,
			27,
			28,
			29,
			MEDALLION_OF_WARRIOR,
			40
		});
		Classes.put("HK", new int[]
		{
			4,
			0,
			0,
			30,
			31,
			32,
			33,
			SWORD_OF_RITUAL,
			40
		});
		Classes.put("HR", new int[]
		{
			7,
			0,
			0,
			34,
			35,
			36,
			37,
			BEZIQUES_RECOMMENDATION,
			40
		});
		Classes.put("HWI", new int[]
		{
			11,
			10,
			0,
			23,
			24,
			25,
			26,
			BEAD_OF_SEASON,
			33
		});
		Classes.put("HC", new int[]
		{
			15,
			10,
			0,
			27,
			28,
			29,
			30,
			MARK_OF_FAITH,
			33
		});
	}
	
	// Also used by ShadowWeapon script.
	public static final int[] FIRSTCLASSNPCS =
	{
		30026,
		30031,
		30037,
		30066,
		30070,
		30154,
		30288,
		30289,
		30290,
		30297,
		30358,
		30373,
		30462,
		30498,
		30499,
		30500,
		30503,
		30504,
		30505,
		30508,
		30520,
		30525,
		30565,
		30594,
		30595,
		32092,
		32093,
		32097,
		32098
	};
	
	public FirstClassChange()
	{
		super(-1, "village_master");
		
		addStartNpc(FIRSTCLASSNPCS);
		addTalkId(FIRSTCLASSNPCS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState("FirstClassChange");
		if (st == null)
			return htmltext;
		
		String suffix = "";
		
		if (Classes.containsKey(event))
		{
			// 0 = newClass, 1 = reqClass, 2 = reqRace, 3 = no/no, 4 = no/ok, 5 = ok/no, 6 = ok/ok, 7 = reqItem, 8 = deniedClass
			final int[] array = Classes.get(event);
			
			if (player.getClassId().getId() == array[1] && player.getRace().ordinal() == array[2])
			{
				final boolean gotItem = st.hasQuestItems(array[7]);
				
				if (player.getLevel() < 20)
					suffix = "-" + ((gotItem) ? array[4] : array[3]);
				else
				{
					if (gotItem)
					{
						suffix = "-" + array[6];
						
						st.takeItems(array[7], 1);
						st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
						st.playSound(QuestState.SOUND_FANFARE);
						
						player.setClassId(array[0]);
						player.setBaseClass(array[0]);
						player.sendPacket(new HennaInfo(player));
						player.broadcastUserInfo();
					}
					else
						suffix = "-" + array[5];
				}
				
				htmltext = npc.getNpcId() + suffix + ".htm";
				st.exitQuest(true);
			}
			else
				htmltext = npc.getNpcId() + "-" + array[8] + ".htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState("FirstClassChange");
		if (st == null)
			return htmltext;
		
		if (player.isSubClassActive())
		{
			st.exitQuest(true);
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		switch (npcId)
		{
			case 30290: // Dark Elf
			case 30297:
			case 30462:
				if (player.getRace() == ClassRace.DARK_ELF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 31)
							htmltext = npcId + "-01.htm";
						else if (player.getClassId().getId() == 38)
							htmltext = npcId + "-08.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-32.htm";
					else
						htmltext = npcId + "-31.htm";
				}
				else
					htmltext = npcId + "-33.htm";
				break;
			
			case 30358: // Thifiell (dark elf)
				if (player.getRace() == ClassRace.DARK_ELF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 31)
							htmltext = npcId + "-01.htm";
						else if (player.getClassId().getId() == 38)
							htmltext = npcId + "-02.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-12.htm";
					else
						htmltext = npcId + "-13.htm";
				}
				else
					htmltext = npcId + "-11.htm";
				break;
			
			case 30500: // Orcs
			case 30505:
			case 30508:
			case 32097:
				if (player.getRace() == ClassRace.ORC)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 44)
							htmltext = npcId + "-01.htm";
						else if (player.getClassId().getId() == 49)
							htmltext = npcId + "-06.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-21.htm";
					else
						htmltext = npcId + "-22.htm";
				}
				else
					htmltext = npcId + "-23.htm";
				break;
			
			case 30565: // Kakai (Orcs)
				if (player.getRace() == ClassRace.ORC)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 44)
							htmltext = npcId + "-01.htm";
						else if (player.getClassId().getId() == 49)
							htmltext = npcId + "-06.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-09.htm";
					else
						htmltext = npcId + "-10.htm";
				}
				else
					htmltext = npcId + "-11.htm";
				break;
			
			case 30503: // Dwarf
			case 30594:
			case 30498:
			case 32092:
			case 32093:
			case 30504:
			case 30595:
			case 30499:
				if (player.getRace() == ClassRace.DWARF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 53)
							htmltext = npcId + "-01.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-09.htm";
					else
						htmltext = npcId + "-10.htm";
				}
				else
					htmltext = npcId + "-11.htm";
				break;
			
			case 30525: // Bronk and Reed(dwarf)
			case 30520:
				if (player.getRace() == ClassRace.DWARF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 53)
							htmltext = npcId + "-01.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-05.htm";
					else
						htmltext = npcId + "-06.htm";
				}
				else
					htmltext = npcId + "-07.htm";
				break;
			
			case 30070: // Elfs and humans mages
			case 30037:
			case 30289:
			case 32098:
				if (player.getRace() == ClassRace.ELF)
				{
					if (player.isMageClass())
					{
						if (player.getClassId().level() == 0)
						{
							if (player.getClassId().getId() == 25)
								htmltext = npcId + "-01.htm";
						}
						else if (player.getClassId().level() == 1)
							htmltext = npcId + "-31.htm";
						else
							htmltext = npcId + "-32.htm";
					}
					else
						htmltext = npcId + "-33.htm";
				}
				else if (player.getRace() == ClassRace.HUMAN)
				{
					if (player.isMageClass())
					{
						if (player.getClassId().level() == 0)
						{
							if (player.getClassId().getId() == 10)
								htmltext = npcId + "-08.htm";
						}
						else if (player.getClassId().level() == 1)
							htmltext = npcId + "-31.htm";
						else
							htmltext = npcId + "-32.htm";
					}
					else
						htmltext = npcId + "-33.htm";
				}
				else
					htmltext = npcId + "-33.htm";
				break;
			
			case 30154: // Asterios (Elf fighters and mages)
				if (player.getRace() == ClassRace.ELF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 18)
							htmltext = npcId + "-01.htm";
						else if (player.getClassId().getId() == 25)
							htmltext = npcId + "-02.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-12.htm";
					else
						htmltext = npcId + "-13.htm";
				}
				else
					htmltext = npcId + "-11.htm";
				break;
			
			case 30031: // Biotin (Human mages)
				if (player.getRace() == ClassRace.HUMAN)
				{
					if (player.isMageClass())
					{
						if (player.getClassId().level() == 0)
						{
							if (player.getClassId().getId() == 10)
								htmltext = npcId + "-01.htm";
						}
						else if (player.getClassId().level() == 1)
							htmltext = npcId + "-06.htm";
						else
							htmltext = npcId + "-07.htm";
					}
					else
						htmltext = npcId + "-08.htm";
				}
				else
					htmltext = npcId + "-08.htm";
				break;
			
			case 30373: // Human and Elfs fighters
			case 30288:
			case 30066:
				if (player.getRace() == ClassRace.HUMAN)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 0)
							htmltext = npcId + "-08.htm";
						else
							htmltext = npcId + "-40.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-38.htm";
					else
						htmltext = npcId + "-39.htm";
				}
				else if (player.getRace() == ClassRace.ELF)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 18)
							htmltext = npcId + "-01.htm";
						else
							htmltext = npcId + "-40.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-38.htm";
					else
						htmltext = npcId + "-39.htm";
				}
				else
					htmltext = npcId + "-40.htm";
				break;
			
			case 30026: // Bitz (Human fighters)
				if (player.getRace() == ClassRace.HUMAN)
				{
					if (player.getClassId().level() == 0)
					{
						if (player.getClassId().getId() == 0)
							htmltext = npcId + "-01.htm";
						else
							htmltext = npcId + "-10.htm";
					}
					else if (player.getClassId().level() == 1)
						htmltext = npcId + "-08.htm";
					else
						htmltext = npcId + "-09.htm";
				}
				else
					htmltext = npcId + "-10.htm";
				break;
		}
		st.exitQuest(true);
		
		return htmltext;
	}
}