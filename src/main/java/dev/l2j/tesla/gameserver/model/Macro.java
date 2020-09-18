package dev.l2j.tesla.gameserver.model;

/**
 * Macros are used to automate some processes, to perform a series of actions or to use several skills at once.
 */
public class Macro
{
	public static final int CMD_TYPE_SKILL = 1;
	public static final int CMD_TYPE_ACTION = 3;
	public static final int CMD_TYPE_SHORTCUT = 4;
	
	public int id;
	public final int icon;
	public final String name;
	public final String descr;
	public final String acronym;
	public final MacroCmd[] commands;
	
	public static class MacroCmd
	{
		public final int entry;
		public final int type;
		public final int d1; // skill_id or page for shortcuts
		public final int d2; // shortcut
		public final String cmd;
		
		public MacroCmd(int pEntry, int pType, int pD1, int pD2, String pCmd)
		{
			entry = pEntry;
			type = pType;
			d1 = pD1;
			d2 = pD2;
			cmd = pCmd;
		}
	}
	
	public Macro(int pId, int pIcon, String pName, String pDescr, String pAcronym, MacroCmd[] pCommands)
	{
		id = pId;
		icon = pIcon;
		name = pName;
		descr = pDescr;
		acronym = pAcronym;
		commands = pCommands;
	}
	
	@Override
	public String toString()
	{
		return "macro id=" + id + ", icon=" + icon + ", name=" + name + ", descr=" + descr + ", acronym=" + acronym;
	}
}