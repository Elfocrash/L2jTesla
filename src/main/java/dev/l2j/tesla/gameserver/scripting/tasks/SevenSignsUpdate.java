package dev.l2j.tesla.gameserver.scripting.tasks;

import dev.l2j.tesla.gameserver.scripting.ScheduledQuest;
import dev.l2j.tesla.gameserver.data.manager.FestivalOfDarknessManager;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;

public final class SevenSignsUpdate extends ScheduledQuest
{
	public SevenSignsUpdate()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		if (!SevenSignsManager.getInstance().isSealValidationPeriod())
			FestivalOfDarknessManager.getInstance().saveFestivalData(false);
		
		SevenSignsManager.getInstance().saveSevenSignsData();
		SevenSignsManager.getInstance().saveSevenSignsStatus();
	}
	
	@Override
	public final void onEnd()
	{
	}
}