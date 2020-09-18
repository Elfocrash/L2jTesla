package dev.l2j.tesla.gameserver.communitybbs.Manager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.communitybbs.BB.Forum;
import dev.l2j.tesla.gameserver.communitybbs.BB.Post;
import dev.l2j.tesla.gameserver.communitybbs.BB.Topic;
import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.enums.TopicType;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class TopicBBSManager extends BaseBBSManager
{
	private final List<Topic> _topics = new ArrayList<>();
	private final Map<Forum, Integer> _maxId = new ConcurrentHashMap<>();
	
	protected TopicBBSManager()
	{
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player)
	{
		if (ar1.equals("crea"))
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (forum == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			forum.vload();
			final Topic topic = new Topic(TopicType.CREATE, TopicBBSManager.getInstance().getMaxID(forum) + 1, Integer.parseInt(ar2), ar5, Calendar.getInstance().getTimeInMillis(), player.getName(), player.getObjectId(), Topic.MEMO, 0);
			forum.addTopic(topic);
			TopicBBSManager.getInstance().setMaxID(topic.getID(), forum);
			
			final Post post = new Post(player.getName(), player.getObjectId(), Calendar.getInstance().getTimeInMillis(), topic.getID(), forum.getId(), ar4);
			PostBBSManager.getInstance().addPostByTopic(post, topic);
			
			parseCmd("_bbsmemo", player);
		}
		else if (ar1.equals("del"))
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (forum == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Topic topic = forum.getTopic(Integer.parseInt(ar3));
			if (topic == null)
			{
				separateAndSend("<html><body><br><br><center>The topic named '" + ar3 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Post post = PostBBSManager.getInstance().getPostByTopic(topic);
			if (post != null)
				post.deleteMe(topic);
			
			topic.deleteMe(forum);
			parseCmd("_bbsmemo", player);
		}
		else
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.equals("_bbsmemo"))
		{
			final Forum forum = player.getMemo();
			if (forum != null)
				showTopics(forum, player, 1, forum.getId());
		}
		else if (command.startsWith("_bbstopics;read"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int forumId = Integer.parseInt(st.nextToken());
			final String index = (st.hasMoreTokens()) ? st.nextToken() : null;
			final int ind = (index == null) ? 1 : Integer.parseInt(index);
			
			showTopics(ForumsBBSManager.getInstance().getForumByID(forumId), player, ind, forumId);
		}
		else if (command.startsWith("_bbstopics;crea"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int forumId = Integer.parseInt(st.nextToken());
			
			showNewTopic(ForumsBBSManager.getInstance().getForumByID(forumId), player, forumId);
		}
		else if (command.startsWith("_bbstopics;del"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int forumId = Integer.parseInt(st.nextToken());
			final int topicId = Integer.parseInt(st.nextToken());
			
			final Forum forum = ForumsBBSManager.getInstance().getForumByID(forumId);
			if (forum == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Topic topic = forum.getTopic(topicId);
			if (topic == null)
			{
				separateAndSend("<html><body><br><br><center>The topic named '" + topicId + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Post post = PostBBSManager.getInstance().getPostByTopic(topic);
			if (post != null)
				post.deleteMe(topic);
			
			topic.deleteMe(forum);
			parseCmd("_bbsmemo", player);
		}
		else
			super.parseCmd(command, player);
	}
	
	public void addTopic(Topic topic)
	{
		_topics.add(topic);
	}
	
	public void deleteTopic(Topic topic)
	{
		_topics.remove(topic);
	}
	
	public void setMaxID(int id, Forum forum)
	{
		_maxId.put(forum, id);
	}
	
	public int getMaxID(Forum forum)
	{
		return _maxId.getOrDefault(forum, 0);
	}
	
	public Topic getTopicById(int forumId)
	{
		return _topics.stream().filter(t -> t.getID() == forumId).findFirst().orElse(null);
	}
	
	private static void showNewTopic(Forum forum, Player player, int forumId)
	{
		if (forum == null)
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
			return;
		}
		
		if (forum.getType() == Forum.MEMO)
			showMemoNewTopics(forum, player);
		else
			separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
	}
	
	private static void showMemoNewTopics(Forum forum, Player player)
	{
		final String html = "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.getId() + " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>";
		send1001(html, player);
		send1002(player);
	}
	
	private void showTopics(Forum forum, Player player, int index, int forumId)
	{
		if (forum == null)
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
			return;
		}
		
		if (forum.getType() == Forum.MEMO)
			showMemoTopics(forum, player, index);
		else
			separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
	}
	
	private void showMemoTopics(Forum forum, Player player, int index)
	{
		forum.vload();
		final StringBuilder sb = new StringBuilder("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415 align=center>&$413;</td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>&$418;</td></tr></table>");
		
		final DateFormat dateFormat = DateFormat.getInstance();
		for (int i = 0, j = getMaxID(forum) + 1; i < 12 * index; j--)
		{
			if (j < 0)
				break;
			
			final Topic topic = forum.getTopic(j);
			if (topic != null)
			{
				if (i++ >= 12 * (index - 1))
					StringUtil.append(sb, "<table border=0 cellspacing=0 cellpadding=5 WIDTH=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415><a action=\"bypass _bbsposts;read;", forum.getId(), ";", topic.getID(), "\">", topic.getName(), "</a></td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>", dateFormat.format(new Date(topic.getDate())), "</td></tr></table><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
			}
		}
		
		sb.append("<br><table width=610 cellspace=0 cellpadding=0><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=510 align=center><table border=0><tr>");
		
		if (index == 1)
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		else
			StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getId(), ";", index - 1, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		
		int pageNumber;
		pageNumber = forum.getTopicSize() / 8;
		if (pageNumber * 8 != ClanTable.getInstance().getClans().size())
			pageNumber++;
		
		for (int i = 1; i <= pageNumber; i++)
		{
			if (i == index)
				StringUtil.append(sb, "<td> ", i, " </td>");
			else
				StringUtil.append(sb, "<td><a action=\"bypass _bbstopics;read;", forum.getId(), ";", i, "\"> ", i, " </a></td>");
		}
		
		if (index == pageNumber)
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		else
			StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getId(), ";", index + 1, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		
		StringUtil.append(sb, "</tr></table></td><td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;", forum.getId(), "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td></td><td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td><td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table></td></tr></table><br><br><br></center></body></html>");
		separateAndSend(sb.toString(), player);
	}
	
	public static TopicBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TopicBBSManager INSTANCE = new TopicBBSManager();
	}
}