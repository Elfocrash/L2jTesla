package dev.l2j.tesla.gameserver.data.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.commons.logging.CLogger;

/**
 * A cache storing clan crests under .dds format.<br>
 * <br>
 * Size integrity checks are made on crest save, deletion, get and also during server first load.
 */
public class CrestCache
{
	public static enum CrestType
	{
		PLEDGE("Crest_", 256),
		PLEDGE_LARGE("LargeCrest_", 2176),
		ALLY("AllyCrest_", 192);
		
		private final String _prefix;
		private final int _size;
		
		private CrestType(String prefix, int size)
		{
			_prefix = prefix;
			_size = size;
		}
		
		public final String getPrefix()
		{
			return _prefix;
		}
		
		public final int getSize()
		{
			return _size;
		}
	}
	
	private static final CLogger LOGGER = new CLogger(CrestCache.class.getName());
	
	private static final String CRESTS_DIR = "./data/crests/";
	
	private final Map<Integer, byte[]> _crests = new HashMap<>();
	private final FileFilter _ddsFilter = new DdsFilter();
	
	public CrestCache()
	{
		load();
	}
	
	/**
	 * Initial method used to load crests data and store it in server memory.<br>
	 * <br>
	 * If a file doesn't meet integrity checks requirements, it is simply deleted.
	 */
	private final void load()
	{
		for (File file : new File(CRESTS_DIR).listFiles())
		{
			final String fileName = file.getName();
			
			// Invalid file type has been found ; delete it.
			if (!_ddsFilter.accept(file))
			{
				file.delete();
				
				LOGGER.warn("Invalid file {} has been deleted while loading crests.", fileName);
				continue;
			}
			
			// Load data on byte array.
			byte[] data;
			try (RandomAccessFile f = new RandomAccessFile(file, "r"))
			{
				data = new byte[(int) f.length()];
				f.readFully(data);
			}
			catch (Exception e)
			{
				LOGGER.error("Error loading crest file: {}.", e, fileName);
				continue;
			}
			
			// Test each crest type.
			for (CrestType type : CrestType.values())
			{
				// We found a matching crest type.
				if (fileName.startsWith(type.getPrefix()))
				{
					// The data size isn't the required one, delete the file.
					if (data.length != type.getSize())
					{
						file.delete();
						
						LOGGER.warn("The data for crest {} is invalid. The crest has been deleted.", fileName);
						continue;
					}
					
					// Feed the cache with crest id as key, and crest data as value.
					_crests.put(Integer.valueOf(fileName.substring(type.getPrefix().length(), fileName.length() - 4)), data);
					continue;
				}
			}
		}
		
		LOGGER.info("Loaded {} crests.", _crests.size());
	}
	
	/**
	 * Cleans the crest cache, and reload it.
	 */
	public final void reload()
	{
		_crests.clear();
		
		load();
	}
	
	/**
	 * @param type : The {@link CrestType} to refer on. Size integrity check is made based on it.
	 * @param id : The crest id data to retrieve.
	 * @return a byte array or null if id wasn't found.
	 */
	public final byte[] getCrest(CrestType type, int id)
	{
		// get crest data
		byte[] data = _crests.get(id);
		
		// crest data is not required type, return
		if (data == null || data.length != type.getSize())
			return null;
		
		return data;
	}
	
	/**
	 * Removes the crest from both memory and file system.
	 * @param type : The {@link CrestType} to refer on. Size integrity check is made based on it.
	 * @param id : The crest id to delete.
	 */
	public final void removeCrest(CrestType type, int id)
	{
		// get crest data
		byte[] data = _crests.get(id);
		
		// crest data is not required type, return
		if (data == null || data.length != type.getSize())
			return;
		
		// remove from cache
		_crests.remove(id);
		
		// delete file
		final File file = new File(CRESTS_DIR + type.getPrefix() + id + ".dds");
		if (!file.delete())
			LOGGER.warn("Error deleting crest file: {}.", file.getName());
	}
	
	/**
	 * Stores the crest as a physical file and in cache memory.
	 * @param type : The {@link CrestType} used to register the crest. Crest name uses it.
	 * @param id : The crest id to register this new crest.
	 * @param data : The crest data to store.
	 * @return true if the crest has been successfully saved, false otherwise.
	 */
	public final boolean saveCrest(CrestType type, int id, byte[] data)
	{
		// Create the file.
		final File file = new File(CRESTS_DIR + type.getPrefix() + id + ".dds");
		
		// Verify the data size integrity.
		if (data.length != type.getSize())
		{
			LOGGER.warn("The data for crest {} is invalid. Saving process is aborted.", file.getName());
			return false;
		}
		
		// Save the crest file with given data.
		try (FileOutputStream out = new FileOutputStream(file))
		{
			out.write(data);
		}
		catch (Exception e)
		{
			LOGGER.error("Error saving crest file: {}.", e, file.getName());
			return false;
		}
		
		// Feed the cache with crest data.
		_crests.put(id, data);
		
		return true;
	}
	
	protected class DdsFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			final String fileName = file.getName();
			
			return (fileName.startsWith("Crest_") || fileName.startsWith("LargeCrest_") || fileName.startsWith("AllyCrest_")) && fileName.endsWith(".dds");
		}
	}
	
	public static CrestCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CrestCache INSTANCE = new CrestCache();
	}
}