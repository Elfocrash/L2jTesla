package dev.l2j.tesla.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.communitybbs.Manager.PostBBSManager;
import dev.l2j.tesla.commons.logging.CLogger;

public class Post
{
	private static final CLogger LOGGER = new CLogger(Post.class.getName());
	
	private static final String RESTORE_POSTS = "SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC";
	private static final String ADD_POST = "INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)";
	private static final String DELETE_POST = "DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?";
	private static final String UPDATE_TEXT = "UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?";
	
	private final List<CPost> _posts = new ArrayList<>();
	
	public Post(Topic topic)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_POSTS))
		{
			ps.setInt(1, topic.getForumID());
			ps.setInt(2, topic.getID());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					_posts.add(new CPost(rs.getInt("post_id"), rs.getString("post_owner_name"), rs.getInt("post_ownerid"), rs.getLong("post_date"), rs.getInt("post_topic_id"), rs.getInt("post_forum_id"), rs.getString("post_txt")));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load posts for {} / {}.", e, topic.getForumID(), topic.getID());
		}
	}
	
	public Post(String owner, int ownerId, long date, int topicId, int forumId, String text)
	{
		// Create a new CPost object.
		final CPost post = new CPost(0, owner, ownerId, date, topicId, forumId, text);
		
		// Add it to the container.
		_posts.add(post);
		
		// Insert it to database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_POST))
		{
			ps.setInt(1, 0);
			ps.setString(2, owner);
			ps.setInt(3, ownerId);
			ps.setLong(4, date);
			ps.setInt(5, topicId);
			ps.setInt(6, forumId);
			ps.setString(7, text);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save new Post.", e);
		}
	}
	
	public CPost getCPost(int id)
	{
		int i = 0;
		for (CPost cp : _posts)
		{
			if (i++ == id)
				return cp;
		}
		return null;
	}
	
	public void deleteMe(Topic topic)
	{
		PostBBSManager.getInstance().deletePostByTopic(topic);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_POST))
		{
			ps.setInt(1, topic.getForumID());
			ps.setInt(2, topic.getID());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete Post.", e);
		}
	}
	
	public void updateText(int index)
	{
		final CPost post = getCPost(index);
		if (post == null)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_TEXT))
		{
			ps.setString(1, post.getText());
			ps.setInt(2, post.getId());
			ps.setInt(3, post.getTopicId());
			ps.setInt(4, post.getForumId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update Post text.", e);
		}
	}
	
	public class CPost
	{
		private final int _id;
		private final String _owner;
		private final int _ownerId;
		private final long _date;
		private final int _topicId;
		private final int _forumId;
		
		private String _text;
		
		public CPost(int id, String owner, int ownerId, long date, int topicId, int forumId, String text)
		{
			_id = id;
			_owner = owner;
			_ownerId = ownerId;
			_date = date;
			_topicId = topicId;
			_forumId = forumId;
			_text = text;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getOwner()
		{
			return _owner;
		}
		
		public int getOwnerId()
		{
			return _ownerId;
		}
		
		public long getDate()
		{
			return _date;
		}
		
		public int getTopicId()
		{
			return _topicId;
		}
		
		public int getForumId()
		{
			return _forumId;
		}
		
		public String getText()
		{
			return _text;
		}
		
		public void setText(String text)
		{
			_text = text;
		}
	}
}