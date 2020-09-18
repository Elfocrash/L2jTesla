package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.network.serverpackets.AcquireSkillInfo;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.xml.SkillTreeData;
import dev.l2j.tesla.gameserver.data.xml.SpellbookData;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Folk;
import dev.l2j.tesla.gameserver.model.holder.skillnode.ClanSkillNode;
import dev.l2j.tesla.gameserver.model.holder.skillnode.FishingSkillNode;
import dev.l2j.tesla.gameserver.model.holder.skillnode.GeneralSkillNode;

public class RequestAcquireSkillInfo extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLevel;
	private int _skillType;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLevel = readD();
		_skillType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// Not valid skill data, return.
		if (_skillId <= 0 || _skillLevel <= 0)
			return;
		
		// Incorrect player, return.
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		// Incorrect npc, return.
		final Folk folk = player.getCurrentFolk();
		if (folk == null || !folk.canInteract(player))
			return;
		
		// Skill doesn't exist, return.
		final L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
		if (skill == null)
			return;
		
		final AcquireSkillInfo asi;
		
		switch (_skillType)
		{
			// General skills
			case 0:
				// Player already has such skill with same or higher level.
				int skillLvl = player.getSkillLevel(_skillId);
				if (skillLvl >= _skillLevel)
					return;
				
				// Requested skill must be 1 level higher than existing skill.
				if (skillLvl != _skillLevel - 1)
					return;
				
				if (!folk.getTemplate().canTeach(player.getClassId()))
					return;
				
				// Search if the asked skill exists on player template.
				final GeneralSkillNode gsn = player.getTemplate().findSkill(_skillId, _skillLevel);
				if (gsn != null)
				{
					asi = new AcquireSkillInfo(_skillId, _skillLevel, gsn.getCorrectedCost(), 0);
					final int bookId = SpellbookData.getInstance().getBookForSkill(_skillId, _skillLevel);
					if (bookId != 0)
						asi.addRequirement(99, bookId, 1, 50);
					sendPacket(asi);
				}
				break;
			
			// Common skills
			case 1:
				// Player already has such skill with same or higher level.
				skillLvl = player.getSkillLevel(_skillId);
				if (skillLvl >= _skillLevel)
					return;
				
				// Requested skill must be 1 level higher than existing skill.
				if (skillLvl != _skillLevel - 1)
					return;
				
				final FishingSkillNode fsn = SkillTreeData.getInstance().getFishingSkillFor(player, _skillId, _skillLevel);
				if (fsn != null)
				{
					asi = new AcquireSkillInfo(_skillId, _skillLevel, 0, 1);
					asi.addRequirement(4, fsn.getItemId(), fsn.getItemCount(), 0);
					sendPacket(asi);
				}
				break;
			
			// Pledge skills.
			case 2:
				if (!player.isClanLeader())
					return;
				
				final ClanSkillNode csn = SkillTreeData.getInstance().getClanSkillFor(player, _skillId, _skillLevel);
				if (csn != null)
				{
					asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), csn.getCost(), 2);
					if (Config.LIFE_CRYSTAL_NEEDED && csn.getItemId() != 0)
						asi.addRequirement(1, csn.getItemId(), 1, 0);
					sendPacket(asi);
				}
				break;
		}
	}
}