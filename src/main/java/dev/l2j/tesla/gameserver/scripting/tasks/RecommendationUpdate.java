package dev.l2j.tesla.gameserver.scripting.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.scripting.ScheduledQuest;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.UserInfo;

public final class RecommendationUpdate extends ScheduledQuest
{
	private static final String DELETE_CHAR_RECOMS = "TRUNCATE TABLE character_recommends";
	private static final String SELECT_ALL_RECOMS = "SELECT obj_Id, level, rec_have FROM characters";
	private static final String UPDATE_ALL_RECOMS = "UPDATE characters SET rec_left=?, rec_have=? WHERE obj_Id=?";
	
	public RecommendationUpdate()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		// Refresh online characters stats.
		for (Player player : World.getInstance().getPlayers())
		{
			player.getRecomChars().clear();
			
			final int level = player.getLevel();
			if (level < 20)
			{
				player.setRecomLeft(3);
				player.editRecomHave(-1);
			}
			else if (level < 40)
			{
				player.setRecomLeft(6);
				player.editRecomHave(-2);
			}
			else
			{
				player.setRecomLeft(9);
				player.editRecomHave(-3);
			}
			
			player.sendPacket(new UserInfo(player));
		}
		
		// Refresh database side.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all characters listed on character_recommends table.
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RECOMS))
			{
				ps.execute();
			}
			
			// Initialize the update statement.
			try (PreparedStatement ps2 = con.prepareStatement(UPDATE_ALL_RECOMS))
			{
				// Select needed informations of all characters.
				try (PreparedStatement ps = con.prepareStatement(SELECT_ALL_RECOMS))
				{
					try (ResultSet rs = ps.executeQuery())
					{
						while (rs.next())
						{
							final int level = rs.getInt("level");
							if (level < 20)
							{
								ps2.setInt(1, 3);
								ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 1));
							}
							else if (level < 40)
							{
								ps2.setInt(1, 6);
								ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 2));
							}
							else
							{
								ps2.setInt(1, 9);
								ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 3));
							}
							ps2.setInt(3, rs.getInt("obj_Id"));
							ps2.addBatch();
						}
					}
				}
				ps2.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't clear players recommendations.", e);
		}
	}
	
	@Override
	public final void onEnd()
	{
	}
}