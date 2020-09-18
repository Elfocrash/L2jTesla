package dev.l2j.tesla.gameserver.skills.basefuncs;

import dev.l2j.tesla.gameserver.skills.conditions.Condition;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.skills.Env;

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).<br>
 * In fact, each calculator is a table of Func object in which each Func represents a mathematics function:<br>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
 * When the calc method of a calculator is launched, each mathematics function is called according to its priority <B>_order</B>.<br>
 * Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order.<br>
 * The result of the calculation is stored in the value property of an Env class instance.
 */
public abstract class Func
{
	/**
	 * Statistics, that is affected by this function (See Creature.CALCULATOR_XXX constants)
	 */
	public final Stats stat;
	
	/**
	 * Order of functions calculation.<br>
	 * Functions with lower order are executed first.<br>
	 * Functions with the same order are executed in unspecified order.<br>
	 * Usually add/subtract functions has lowest order,<br>
	 * then bonus/penalty functions (multiply/divide) are applied, then functions that do more complex<br>
	 * calculations (non-linear functions).
	 */
	public final int order;
	
	/**
	 * Owner can be an armor, weapon, skill, system event, quest, etc.<br>
	 * Used to remove all functions added by this owner.
	 */
	public final Object funcOwner;
	
	/**
	 * Function may be disabled by attached condition.
	 */
	public Condition cond;
	
	public Lambda _lambda;
	
	/**
	 * Constructor of Func.
	 * @param pStat
	 * @param pOrder
	 * @param owner
	 * @param lambda
	 */
	public Func(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = owner;
		_lambda = lambda;
	}
	
	/**
	 * Add a condition to the Func.
	 * @param pCond
	 */
	public void setCondition(Condition pCond)
	{
		cond = pCond;
	}
	
	/**
	 * Run the mathematics function of the Func.
	 * @param env
	 */
	public abstract void calc(Env env);
}
