package dev.l2j.tesla.gameserver.communitybbs.Manager;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.communitybbs.BB.Forum;
import dev.l2j.tesla.gameserver.communitybbs.BB.Post;
import dev.l2j.tesla.gameserver.communitybbs.BB.Topic;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class PostBBSManager extends BaseBBSManager
{
	private final Map<Topic, Post> _postByTopic = new HashMap<>();
	
	protected PostBBSManager()
	{
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.startsWith("_bbsposts;read;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int forumId = Integer.parseInt(st.nextToken());
			final int pageNumber = Integer.parseInt(st.nextToken());
			final int index = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
			
			showPost((TopicBBSManager.getInstance().getTopicById(pageNumber)), ForumsBBSManager.getInstance().getForumByID(forumId), player, index);
		}
		else if (command.startsWith("_bbsposts;edit;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int forumId = Integer.parseInt(st.nextToken());
			final int topicId = Integer.parseInt(st.nextToken());
			final int pageId = Integer.parseInt(st.nextToken());
			
			showEditPost((TopicBBSManager.getInstance().getTopicById(topicId)), ForumsBBSManager.getInstance().getForumByID(forumId), player, pageId);
		}
		else
			super.parseCmd(command, player);
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player)
	{
		final StringTokenizer st = new StringTokenizer(ar1, ";");
		
		final int forumId = Integer.parseInt(st.nextToken());
		final int topicId = Integer.parseInt(st.nextToken());
		final int pageId = Integer.parseInt(st.nextToken());
		
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
		
		final Post post = getPostByTopic(topic);
		if (post.getCPost(pageId) == null)
		{
			separateAndSend("<html><body><br><br><center>The post named '" + pageId + "' doesn't exist.</center></body></html>", player);
			return;
		}
		
		post.getCPost(pageId).setText(ar4);
		post.updateText(pageId);
		
		parseCmd("_bbsposts;read;" + forum.getId() + ";" + topic.getID(), player);
	}
	
	public Post getPostByTopic(Topic topic)
	{
		Post post = _postByTopic.get(topic);
		if (post == null)
		{
			post = load(topic);
			_postByTopic.put(topic, post);
		}
		return post;
	}
	
	public void deletePostByTopic(Topic topic)
	{
		_postByTopic.remove(topic);
	}
	
	public void addPostByTopic(Post post, Topic topic)
	{
		if (_postByTopic.get(topic) == null)
			_postByTopic.put(topic, post);
	}
	
	private static Post load(Topic topic)
	{
		return new Post(topic);
	}
	
	private void showEditPost(Topic topic, Forum forum, Player player, int pageNumber)
	{
		if (forum == null || topic == null)
		{
			separateAndSend("<html><body><br><br><center>This forum and/or topic don't exit.</center></body></html>", player);
			return;
		}
		
		final Post post = getPostByTopic(topic);
		if (post == null)
		{
			separateAndSend("<html><body><br><br><center>This post doesn't exist.</center></body></html>", player);
			return;
		}
		
		showHtmlEditPost(topic, player, forum, post);
	}
	
	private void showPost(Topic topic, Forum forum, Player player, int ind)
	{
		if (forum == null || topic == null)
			separateAndSend("<html><body><br><br><center>This forum and/or topic don't exist.</center></body></html>", player);
		else if (forum.getType() == Forum.MEMO)
			showMemoPost(topic, player, forum);
		else
			separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' isn't implemented.</center></body></html>", player);
	}
	
	private static void showHtmlEditPost(Topic topic, Player player, Forum forum, Post post)
	{
		final String html = "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">" + forum.getName() + " Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540>" + topic.getName() + "</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post " + forum.getId() + ";" + topic.getID() + ";0 _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>";
		send1001(html, player);
		send1002(player, post.getCPost(0).getText(), topic.getName(), DateFormat.getInstance().format(new Date(topic.getDate())));
	}
	
	private void showMemoPost(Topic topic, Player player, Forum forum)
	{
		Post post = getPostByTopic(topic);
		Locale locale = Locale.getDefault();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		
		String mes = post.getCPost(0).getText().replace(">", "&gt;");
		mes = mes.replace("<", "&lt;");
		mes = mes.replace("\n", "<br1>");
		
		final String html = "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0 bgcolor=333333><tr><td height=10></td></tr><tr><td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td><td fixWIDTH=380 valign=top>" + topic.getName() + "</td><td fixwidth=5></td><td fixwidth=50></td><td fixWIDTH=120></td></tr><tr><td height=10></td></tr><tr><td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td><td><font color=\"AAAAAA\">" + topic.getOwnerName() + "</font></td><td></td><td><font color=\"AAAAAA\">&$418; :</font></td><td><font color=\"AAAAAA\">" + dateFormat.format(post.getCPost(0).getDate()) + "</font></td></tr><tr><td height=10></td></tr></table><br><table border=0 cellspacing=0 cellpadding=0><tr><td fixwidth=5></td><td FIXWIDTH=600 align=left>" + mes + "</td><td fixqqwidth=5></td></tr></table><br><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><img src=\"L2UI.squaregray\" width=\"610\" height=\"1\"><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=560 align=right><table border=0 cellspacing=0><tr><td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;" + forum.getId() + ";" + topic.getID() + ";0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;" + forum.getId() + ";" + topic.getID() + "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;" + forum.getId() + "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;</tr></table></td></tr></table><br><br><br></center></body></html>";
		separateAndSend(html, player);
	}
	
	public static PostBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PostBBSManager INSTANCE = new PostBBSManager();
	}
}