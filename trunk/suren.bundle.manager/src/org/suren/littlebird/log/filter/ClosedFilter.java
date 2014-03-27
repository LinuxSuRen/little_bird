package org.suren.littlebird.log.filter;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class ClosedFilter extends Filter
{

	@Override
	public int decide(LoggingEvent event)
	{
		return Filter.DENY;
	}
}
