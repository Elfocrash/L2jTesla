package dev.l2j.tesla.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.communitybbs.Manager.ForumsBBSManager;
import dev.l2j.tesla.gameserver.communitybbs.Manager.TopicBBSManager;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.enums.TopicType;

public class Forum
{
	private static final CLogger LOGGER = new CLogger(Forum.class.getName());
	
	private static final String RESTORE_FORUMS = "SELECT * FROM forums WHERE forum_id=?";
	private static final String RESTORE_TOPICS = "SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC";
	private static final String RESTORE_CHILDREN = "SELECT forum_id FROM forums WHERE forum_parent=?";
	private static final String ADD_FORUM = "INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)";
	
	// Types
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	
	// Permissions
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;
	
	private final List<Forum> _children = new ArrayList<>();
	private final Map<Integer, Topic> _topics = new HashMap<>();
	
	private final int _forumId;
	private String _forumName;
	
	private int _forumType;
	private int _forumPost;
	private int _forumPerm;
	
	private final Forum _parent;
	
	private int _ownerId;
	
	private boolean _loaded = false;
	
	public Forum(int forumId, Forum parent)
	{
		_forumId = forumId;
		_parent = parent;
	}
	
	public Forum(String name, Forum parent, int type, int perm, int ownerId)
	{
		_forumName = name;
		_forumId = ForumsBBSManager.getInstance().getANewID();
		
		_forumType = type;
		_forumPost = 0;
		_forumPerm = perm;
		_parent = parent;
		_ownerId = ownerId;
		
		parent._children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		_loaded = true;
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_FORUMS);
             PreparedStatement ps2 = con.prepareStatement(RESTORE_TOPICS))
		{
			ps.setInt(1, _forumId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					_forumName = rs.getString("forum_name");
					
					_forumPost = rs.getInt("forum_post");
					_forumType = rs.getInt("forum_type");
					_forumPerm = rs.getInt("forum_perm");
					
					_ownerId = rs.getInt("forum_owner_id");
				}
			}
			
			ps2.setInt(1, _forumId);
			
			try (ResultSet rs2 = ps2.executeQuery())
			{
				while (rs2.next())
				{
					final Topic topic = new Topic(TopicType.RESTORE, rs2.getInt("topic_id"), rs2.getInt("topic_forum_id"), rs2.getString("topic_name"), rs2.getLong("topic_date"), rs2.getString("topic_ownername"), rs2.getInt("topic_ownerid"), rs2.getInt("topic_type"), rs2.getInt("topic_reply"));
					
					_topics.put(topic.getID(), topic);
					
					if (topic.getID() > TopicBBSManager.getInstance().getMaxID(this))
						TopicBBSManager.getInstance().setMaxID(topic.getID(), this);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load forums with id {}.", e, _forumId);
		}
	}
	
	private void getChildren()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_CHILDREN))
		{
			ps.setInt(1, _forumId);
			
			try (ResultSet result = ps.executeQuery())
			{
				while (result.next())
				{
					final Forum forum = new Forum(result.getInt("forum_id"), this);
					
					_children.add(forum);
					
					ForumsBBSManager.getInstance().addForum(forum);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load children forum for parentId {}.", e, _forumId);
		}
	}
	
	public int getTopicSize()
	{
		vload();
		
		return _topics.size();
	}
	
	public Topic getTopic(int id)
	{
		vload();
		
		return _topics.get(id);
	}
	
	public void addTopic(Topic topic)
	{
		vload();
		
		_topics.put(topic.getID(), topic);
	}
	
	public int getId()
	{
		return _forumId;
	}
	
	public String getName()
	{
		vload();
		
		return _forumName;
	}
	
	public int getType()
	{
		vload();
		
		return _forumType;
	}
	
	public Forum getChildByName(String name)
	{
		vload();
		
		return _children.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
	}
	
	public void removeTopic(int id)
	{
		_topics.remove(id);
	}
	
	public void insertIntoDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_FORUM))
		{
			ps.setInt(1, _forumId);
			ps.setString(2, _forumName);
			ps.setInt(3, _parent.getId());
			ps.setInt(4, _forumPost);
			ps.setInt(5, _forumType);
			ps.setInt(6, _forumPerm);
			ps.setInt(7, _ownerId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save new Forum.", e);
		}
	}
	
	public void vload()
	{
		if (!_loaded)
		{
			load();
			getChildren();
			
			_loaded = true;
		}
	}
}