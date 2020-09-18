package dev.l2j.tesla.commons.logging.formatter;

import java.util.logging.LogRecord;

import dev.l2j.tesla.commons.logging.MasterFormatter;

public class GMAuditFormatter extends MasterFormatter
{
	@Override
	public String format(LogRecord record)
	{
		return "[" + getFormatedDate(record.getMillis()) + "]" + SPACE + record.getMessage() + CRLF;
	}
}