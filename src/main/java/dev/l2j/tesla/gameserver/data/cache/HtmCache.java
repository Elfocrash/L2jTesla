package dev.l2j.tesla.gameserver.data.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.commons.io.UnicodeReader;
import dev.l2j.tesla.commons.logging.CLogger;

/**
 * A cache storing HTMs content.<br>
 * <br>
 * HTMs are loaded lazily, on request, then their {@link String} content can be retrieved using path hashcode.
 */
public class HtmCache
{
	private static final CLogger LOGGER = new CLogger(HtmCache.class.getName());
	
	private final Map<Integer, String> _htmCache = new HashMap<>();
	private final FileFilter _htmFilter = new HtmFilter();
	
	protected HtmCache()
	{
	}
	
	/**
	 * Cleans the HTM cache.
	 */
	public void reload()
	{
		LOGGER.info("HtmCache has been cleared ({} entries).", _htmCache.size());
		
		_htmCache.clear();
	}
	
	/**
	 * Loads and stores the HTM file content.
	 * @param file : The file to be cached.
	 * @return the content of the file under a {@link String}.
	 */
	private String loadFile(File file)
	{
		try (FileInputStream fis = new FileInputStream(file);
			UnicodeReader ur = new UnicodeReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(ur))
		{
			final StringBuilder sb = new StringBuilder();
			
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line).append('\n');
			
			final String content = sb.toString().replaceAll("\r\n", "\n");
			
			_htmCache.put(file.getPath().replace("\\", "/").hashCode(), content);
			return content;
		}
		catch (Exception e)
		{
			LOGGER.error("Error caching HTM file.", e);
			return null;
		}
	}
	
	/**
	 * Check if an HTM exists and can be loaded. If so, it is loaded and stored.
	 * @param path : The path to the HTM.
	 * @return true if the HTM can be loaded.
	 */
	public boolean isLoadable(String path)
	{
		final File file = new File(path);
		if (!_htmFilter.accept(file))
			return false;
		
		return loadFile(file) != null;
	}
	
	/**
	 * Returns the HTM content given by filename. Test the cache first, then try to load the file if unsuccessful.
	 * @param path : The path to the HTM.
	 * @return the {@link String} content if filename exists, otherwise returns null.
	 */
	public String getHtm(String path)
	{
		if (path == null || path.isEmpty())
			return null;
		
		String content = _htmCache.get(path.hashCode());
		if (content == null)
		{
			final File file = new File(path);
			if (_htmFilter.accept(file))
				content = loadFile(file);
		}
		
		return content;
	}
	
	/**
	 * Return content of html message given by filename. In case filename does not exist, returns notice.
	 * @param path : The path to the HTM.
	 * @return the {@link String} content if filename exists, otherwise returns formatted default message.
	 */
	public String getHtmForce(String path)
	{
		String content = getHtm(path);
		if (content == null)
		{
			content = "<html><body>My html is missing:<br>" + path + "</body></html>";
			LOGGER.warn("Following HTM {} is missing.", path);
		}
		
		return content;
	}
	
	protected class HtmFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			return file.isFile() && (file.getName().endsWith(".htm") || file.getName().endsWith(".html"));
		}
	}
	
	public static HtmCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HtmCache INSTANCE = new HtmCache();
	}
}