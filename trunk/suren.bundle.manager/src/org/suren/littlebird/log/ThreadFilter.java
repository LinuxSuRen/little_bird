package org.suren.littlebird.log;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class ThreadFilter extends Filter
{
	private String	threadName;

	@Override
	public int decide(LoggingEvent event)
	{
		String name = event.getThreadName();

		if(name.equals(getThreadName()))
		{
			return Filter.ACCEPT;
		}
		else
		{
			return Filter.DENY;
		}
	}

	public String getThreadName()
	{
		return threadName;
	}

	public void setThreadName(String threadName)
	{
		this.threadName = threadName;
	}

}
