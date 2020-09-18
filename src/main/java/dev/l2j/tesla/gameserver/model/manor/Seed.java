package dev.l2j.tesla.gameserver.model.manor;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.model.item.kind.Item;

public final class Seed
{
	private final int _seedId;
	private final int _cropId;
	private final int _level;
	private final int _matureId;
	private final int _reward1;
	private final int _reward2;
	private final int _castleId;
	private final boolean _isAlternative;
	private final int _limitSeeds;
	private final int _limitCrops;
	private final int _seedReferencePrice;
	private final int _cropReferencePrice;
	
	public Seed(StatsSet set)
	{
		_seedId = set.getInteger("id");
		_cropId = set.getInteger("cropId");
		_level = set.getInteger("level");
		_matureId = set.getInteger("matureId");
		_reward1 = set.getInteger("reward1");
		_reward2 = set.getInteger("reward2");
		_castleId = set.getInteger("castleId");
		_isAlternative = set.getBool("isAlternative");
		_limitCrops = set.getInteger("cropsLimit");
		_limitSeeds = set.getInteger("seedsLimit");
		
		Item item = ItemTable.getInstance().getTemplate(_cropId);
		_cropReferencePrice = (item != null) ? item.getReferencePrice() : 1;
		
		item = ItemTable.getInstance().getTemplate(_seedId);
		_seedReferencePrice = (item != null) ? item.getReferencePrice() : 1;
	}
	
	public final int getCastleId()
	{
		return _castleId;
	}
	
	public final int getSeedId()
	{
		return _seedId;
	}
	
	public final int getCropId()
	{
		return _cropId;
	}
	
	public final int getMatureId()
	{
		return _matureId;
	}
	
	public final int getReward(int type)
	{
		return (type == 1) ? _reward1 : _reward2;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public final boolean isAlternative()
	{
		return _isAlternative;
	}
	
	public final int getSeedLimit()
	{
		return _limitSeeds * Config.RATE_DROP_MANOR;
	}
	
	public final int getCropLimit()
	{
		return _limitCrops * Config.RATE_DROP_MANOR;
	}
	
	public final int getSeedReferencePrice()
	{
		return _seedReferencePrice;
	}
	
	public final int getSeedMaxPrice()
	{
		return _seedReferencePrice * 10;
	}
	
	public final int getSeedMinPrice()
	{
		return (int) (_seedReferencePrice * 0.6);
	}
	
	public final int getCropReferencePrice()
	{
		return _cropReferencePrice;
	}
	
	public final int getCropMaxPrice()
	{
		return _cropReferencePrice * 10;
	}
	
	public final int getCropMinPrice()
	{
		return (int) (_cropReferencePrice * 0.6);
	}
	
	@Override
	public final String toString()
	{
		return "SeedData [_id=" + _seedId + ", _level=" + _level + ", _crop=" + _cropId + ", _mature=" + _matureId + ", _type1=" + _reward1 + ", _type2=" + _reward2 + ", _manorId=" + _castleId + ", _isAlternative=" + _isAlternative + ", _limitSeeds=" + _limitSeeds + ", _limitCrops=" + _limitCrops + "]";
	}
}