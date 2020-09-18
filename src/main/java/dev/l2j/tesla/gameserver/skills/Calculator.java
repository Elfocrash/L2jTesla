package dev.l2j.tesla.gameserver.skills;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;

/**
 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR>
 * <BR>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
 * <BR>
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
 * value property of an Env class instance.<BR>
 * <BR>
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.<BR>
 * <BR>
 */
public final class Calculator
{
	/** Empty Func table definition */
	private static final Func[] _emptyFuncs = new Func[0];
	
	/** Table of Func object */
	private Func[] _functions;
	
	public Calculator()
	{
		_functions = _emptyFuncs;
	}
	
	public Calculator(Calculator c)
	{
		_functions = c._functions;
	}
	
	/**
	 * @return the number of Funcs in the Calculator.
	 */
	public int size()
	{
		return _functions.length;
	}
	
	/**
	 * Add a Func to the Calculator.
	 * @param f
	 */
	public synchronized void addFunc(Func f)
	{
		Func[] funcs = _functions;
		Func[] tmp = new Func[funcs.length + 1];
		
		final int order = f.order;
		int i;
		
		for (i = 0; i < funcs.length && order >= funcs[i].order; i++)
			tmp[i] = funcs[i];
		
		tmp[i] = f;
		
		for (; i < funcs.length; i++)
			tmp[i + 1] = funcs[i];
		
		_functions = tmp;
	}
	
	/**
	 * Remove a Func from the Calculator.
	 * @param f
	 */
	public synchronized void removeFunc(Func f)
	{
		Func[] funcs = _functions;
		Func[] tmp = new Func[funcs.length - 1];
		
		int i;
		
		for (i = 0; i < funcs.length && f != funcs[i]; i++)
			tmp[i] = funcs[i];
		
		if (i == funcs.length)
			return;
		
		for (i++; i < funcs.length; i++)
			tmp[i - 1] = funcs[i];
		
		if (tmp.length == 0)
			_functions = _emptyFuncs;
		else
			_functions = tmp;
	}
	
	/**
	 * Remove each Func with the specified owner of the Calculator.
	 * @param owner
	 * @return a list containing all left stats.
	 */
	public synchronized List<Stats> removeOwner(Object owner)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func func : _functions)
		{
			if (func.funcOwner == owner)
			{
				modifiedStats.add(func.stat);
				removeFunc(func);
			}
		}
		return modifiedStats;
	}
	
	/**
	 * Run each Func of the Calculator.
	 * @param env
	 */
	public void calc(Env env)
	{
		for (Func func : _functions)
			func.calc(env);
	}
}