package dev.l2j.tesla.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.communitybbs.Manager.TopicBBSManager;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.enums.TopicType;

public class Topic
{
	private static final CLogger LOGGER = new CLogger(Topic.class.getName());
	
	private static final String INSERT_TOPIC = "INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)";
	private static final String DELETE_TOPIC = "DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?";
	
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	
	private final int _id;
	private final int _forumId;
	private final String _topicName;
	private final long _date;
	private final String _ownerName;
	private final int _ownerId;
	private final int _type;
	private final int _cReply;
	
	public Topic(TopicType constructorType, int id, int forumId, String name, long date, String ownerName, int ownerId, int type, int Creply)
	{
		_id = id;
		_forumId = forumId;
		_topicName = name;
		_date = date;
		_ownerName = ownerName;
		_ownerId = ownerId;
		_type = type;
		_cReply = Creply;
		
		TopicBBSManager.getInstance().addTopic(this);
		
		if (constructorType == TopicType.CREATE)
			insertIntoDb();
	}
	
	public int getID()
	{
		return _id;
	}
	
	public int getForumID()
	{
		return _forumId;
	}
	
	public String getName()
	{
		return _topicName;
	}
	
	public String getOwnerName()
	{
		return _ownerName;
	}
	
	public long getDate()
	{
		return _date;
	}
	
	private void insertIntoDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_TOPIC))
		{
			ps.setInt(1, _id);
			ps.setInt(2, _forumId);
			ps.setString(3, _topicName);
			ps.setLong(4, _date);
			ps.setString(5, _ownerName);
			ps.setInt(6, _ownerId);
			ps.setInt(7, _type);
			ps.setInt(8, _cReply);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save new topic.", e);
		}
	}
	
	public void deleteMe(Forum forum)
	{
		TopicBBSManager.getInstance().deleteTopic(this);
		forum.removeTopic(_id);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_TOPIC))
		{
			ps.setInt(1, _id);
			ps.setInt(2, forum.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete topic.", e);
		}
	}
}