package dev.l2j.tesla.gameserver.model.holder;

/**
 * A container used for schemes buffer.
 */
public final class BuffSkillHolder extends IntIntHolder
{
	private final String _type;
	private final String _description;
	
	public BuffSkillHolder(int id, int price, String type, String description)
	{
		super(id, price);
		
		_type = type;
		_description = description;
	}
	
	public final String getType()
	{
		return _type;
	}
	
	public final String getDescription()
	{
		return _description;
	}
}