package dev.l2j.tesla.gameserver.model;

import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.xml.AdminData;

/**
 * A datatype used to retain access level informations, such as isGM() and multiple allowed actions (experience gain, allow transactions/peace attack/damage dealing/...).
 */
public class AccessLevel
{
	private int _accessLevel;
	
	private String _name;
	
	private int _childLevel;
	private AccessLevel _childAccess;
	
	private int _nameColor;
	private int _titleColor;
	
	private boolean _isGm;
	private boolean _allowPeaceAttack;
	private boolean _allowFixedRes;
	private boolean _allowTransaction;
	private boolean _allowAltG;
	private boolean _giveDamage;
	private boolean _takeAggro;
	private boolean _gainExp;
	
	public AccessLevel(StatsSet set)
	{
		_accessLevel = set.getInteger("level");
		_name = set.getString("name");
		_nameColor = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"));
		_titleColor = Integer.decode("0x" + set.getString("titleColor", "FFFF77"));
		_childLevel = set.getInteger("childLevel", 0);
		_isGm = set.getBool("isGM", false);
		_allowPeaceAttack = set.getBool("allowPeaceAttack", false);
		_allowFixedRes = set.getBool("allowFixedRes", false);
		_allowTransaction = set.getBool("allowTransaction", true);
		_allowAltG = set.getBool("allowAltg", false);
		_giveDamage = set.getBool("giveDamage", true);
		_takeAggro = set.getBool("takeAggro", true);
		_gainExp = set.getBool("gainExp", true);
	}
	
	/**
	 * @return the {@link AccessLevel} level.
	 */
	public int getLevel()
	{
		return _accessLevel;
	}
	
	/**
	 * @return the {@link AccessLevel} name.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return the name color of the {@link AccessLevel}.
	 */
	public int getNameColor()
	{
		return _nameColor;
	}
	
	/**
	 * @return the title color of the {@link AccessLevel}.
	 */
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	/**
	 * @return true if the {@link AccessLevel} has gm access or not.
	 */
	public boolean isGm()
	{
		return _isGm;
	}
	
	/**
	 * @return true if the {@link AccessLevel} is allowed to attack in peace zone or not.
	 */
	public boolean allowPeaceAttack()
	{
		return _allowPeaceAttack;
	}
	
	/**
	 * @return true if the {@link AccessLevel} is allowed to use fixed res or not.
	 */
	public boolean allowFixedRes()
	{
		return _allowFixedRes;
	}
	
	/**
	 * @return true if the {@link AccessLevel} is allowed to perform transactions or not.
	 */
	public boolean allowTransaction()
	{
		return _allowTransaction;
	}
	
	/**
	 * @return true if the {@link AccessLevel} is allowed to use AltG commands or not.
	 */
	public boolean allowAltG()
	{
		return _allowAltG;
	}
	
	/**
	 * @return true if the {@link AccessLevel} can give damage or not.
	 */
	public boolean canGiveDamage()
	{
		return _giveDamage;
	}
	
	/**
	 * @return true if the {@link AccessLevel} can take aggro or not.
	 */
	public boolean canTakeAggro()
	{
		return _takeAggro;
	}
	
	/**
	 * @return true if the {@link AccessLevel} can gain exp or not.
	 */
	public boolean canGainExp()
	{
		return _gainExp;
	}
	
	/**
	 * Set the child of a {@link AccessLevel} if not existing, then verify if this access level is associated to AccessLevel set as parameter.
	 * @param access : The AccessLevel to check.
	 * @return true if a child access level is equals to access, otherwise false.
	 */
	public boolean hasChildAccess(AccessLevel access)
	{
		// No child access has been found ; we check if a child level has been set. If yes, then we dig into AdminData to find back the AccessLevel and we set it for future usage.
		if (_childAccess == null && _childLevel > 0)
			_childAccess = AdminData.getInstance().getAccessLevel(_childLevel);
		
		return _childAccess != null && (_childAccess.getLevel() == access.getLevel() || _childAccess.hasChildAccess(access));
	}
}