package dev.l2j.tesla.gameserver.model.actor.instance;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.data.xml.NpcData;

/**
 * This class manages all chest.
 * @author Julian
 */
public final class Chest extends Monster
{
	private volatile boolean _isInteracted;
	private volatile boolean _specialDrop;
	
	public Chest(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsNoRndWalk(true);
		
		_isInteracted = false;
		_specialDrop = false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_isInteracted = false;
		_specialDrop = false;
	}
	
	public boolean isInteracted()
	{
		return _isInteracted;
	}
	
	public void setInteracted()
	{
		_isInteracted = true;
	}
	
	public boolean isSpecialDrop()
	{
		return _specialDrop;
	}
	
	public void setSpecialDrop()
	{
		_specialDrop = true;
	}
	
	@Override
	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker)
	{
		int id = getTemplate().getNpcId();
		
		if (!_specialDrop)
		{
			if (id >= 18265 && id <= 18286)
				id += 3536;
			else if (id == 18287 || id == 18288)
				id = 21671;
			else if (id == 18289 || id == 18290)
				id = 21694;
			else if (id == 18291 || id == 18292)
				id = 21717;
			else if (id == 18293 || id == 18294)
				id = 21740;
			else if (id == 18295 || id == 18296)
				id = 21763;
			else if (id == 18297 || id == 18298)
				id = 21786;
		}
		
		super.doItemDrop(NpcData.getInstance().getTemplate(id), lastAttacker);
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		if (super.isMovementDisabled())
			return true;
		
		if (isInteracted())
			return false;
		
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}