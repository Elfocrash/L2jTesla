package dev.l2j.tesla.commons.logging;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MasterFormatter extends Formatter
{
	protected static final String SHIFT = "\tat ";
	protected static final String CRLF = "\r\n";
	protected static final String SPACE = "\t";
	
	@Override
	public String format(LogRecord record)
	{
		return null;
	}
	
	protected static final String getFormatedDate(long timestamp)
	{
		return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp);
	}
}