package dev.l2j.tesla.gameserver.data.manager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.buylist.NpcBuyList;
import dev.l2j.tesla.gameserver.model.buylist.Product;
import dev.l2j.tesla.gameserver.taskmanager.BuyListTaskManager;
import dev.l2j.tesla.commons.data.xml.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * Loads and stores {@link NpcBuyList}, which is the most common way to show/sell items, with multisell.<br>
 * <br>
 * NpcBuyList owns a list of {@link Product}. Each of them can have a count, making the item acquisition impossible until the next restock timer (stored as SQL data). The count timer is stored on a global task, called {@link BuyListTaskManager}.
 */
public class BuyListManager implements IXmlReader
{
	private final Map<Integer, NpcBuyList> _buyLists = new HashMap<>();
	
	protected BuyListManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/buyLists.xml");
		LOGGER.info("Loaded {} buyLists.", _buyLists.size());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM `buylists`");
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int buyListId = rs.getInt("buylist_id");
				final int itemId = rs.getInt("item_id");
				final int count = rs.getInt("count");
				final long nextRestockTime = rs.getLong("next_restock_time");
				
				final NpcBuyList buyList = _buyLists.get(buyListId);
				if (buyList == null)
					continue;
				
				final Product product = buyList.getProductByItemId(itemId);
				if (product == null)
					continue;
				
				BuyListTaskManager.getInstance().test(product, count, nextRestockTime);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load buyList data from database.", e);
		}
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "buyList", buyListNode ->
		{
			final NamedNodeMap attrs = buyListNode.getAttributes();
			final int buyListId = parseInteger(attrs, "id");
			final NpcBuyList buyList = new NpcBuyList(buyListId);
			buyList.setNpcId(parseInteger(attrs, "npcId"));
			forEach(buyListNode, "product", productNode -> buyList.addProduct(new Product(buyListId, parseAttributes(productNode))));
			_buyLists.put(buyListId, buyList);
		}));
	}
	
	public NpcBuyList getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}
	
	public List<NpcBuyList> getBuyListsByNpcId(int npcId)
	{
		return _buyLists.values().stream().filter(b -> b.isNpcAllowed(npcId)).collect(Collectors.toList());
	}
	
	public static BuyListManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BuyListManager INSTANCE = new BuyListManager();
	}
}