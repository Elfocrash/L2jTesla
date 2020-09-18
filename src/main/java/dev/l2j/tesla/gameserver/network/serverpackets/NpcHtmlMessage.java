package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes VOLUMN UNKNOWN UL U TT TR TITLE TEXTCODE TEXTAREA TD TABLE SUP SUB STRIKE SPIN SELECT RIGHT PRE P OPTION OL MULTIEDIT LI LEFT INPUT IMG I HTML H7 H6 H5 H4 H3 H2 H1 FONT EXTEND EDIT COMMENT COMBOBOX CENTER
 * BUTTON BR BODY BAR ADDRESS A SEL LIST VAR FORE READONL ROWS VALIGN FIXWIDTH BORDERCOLORLI BORDERCOLORDA BORDERCOLOR BORDER BGCOLOR BACKGROUND ALIGN VALU READONLY MULTIPLE SELECTED TYP TYPE MAXLENGTH CHECKED SRC Y X QUERYDELAY NOSCROLLBAR IMGSRC B FG SIZE FACE COLOR DEFFON DEFFIXEDFONT WIDTH VALUE
 * TOOLTIP NAME MIN MAX HEIGHT DISABLED ALIGN MSG LINK HREF ACTION
 */
public final class NpcHtmlMessage extends L2GameServerPacket
{
	private final int _npcObjId;
	private String _html;
	private int _itemId = 0;
	private boolean _validate = true;
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	@Override
	public void runImpl()
	{
		if (!_validate)
			return;
		
		Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		activeChar.clearBypass();
		for (int i = 0; i < _html.length(); i++)
		{
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf("\"", start + 1);
			if (start < 0 || finish < 0)
				break;
			
			if (_html.substring(start + 8, start + 10).equals("-h"))
				start += 11;
			else
				start += 8;
			
			i = finish;
			int finish2 = _html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			else
				activeChar.addBypass(_html.substring(start, finish).trim());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);
		
		writeD(_npcObjId);
		writeS(_html);
		writeD(_itemId);
	}
	
	public void disableValidation()
	{
		_validate = false;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public void setHtml(String text)
	{
		if (text.length() > 8192)
		{
			_html = "<html><body>Html was too long.</body></html>";
			LOGGER.warn("An html content was too long.");
			return;
		}
		_html = text;
	}
	
	public void setFile(String filename)
	{
		setHtml(HtmCache.getInstance().getHtmForce(filename));
	}
	
	public void basicReplace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}
	
	public void replace(String pattern, int value)
	{
		_html = _html.replaceAll(pattern, Integer.toString(value));
	}
	
	public void replace(String pattern, long value)
	{
		_html = _html.replaceAll(pattern, Long.toString(value));
	}
	
	public void replace(String pattern, double value)
	{
		_html = _html.replaceAll(pattern, Double.toString(value));
	}
}