package org.suren.littlebird.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

public class DefaultLoggerServer implements LoggerServer
{
	private static final long	serialVersionUID	= 1L;

	public String getName()
	{
		return "logger";
	}

	public List<String> getNames()
	{
		Enumeration<Logger> loggers = getLoggers();

		List<String> names = new ArrayList<String>();
		while(loggers.hasMoreElements())
		{
			names.add(loggers.nextElement().getName());
		}

		return names;
	}

	public List<String> searchBy(String search)
	{
		List<String> names = getNames();
		for(int i = 0; i < names.size();)
		{
			String name = names.get(i);

			if(name.contains(search))
			{
				i++;
			}
			else
			{
				names.remove(i);
			}
		}

		return names;
	}

	public String getLevel(String name)
	{
		Logger logger = LogManager.getLogger(name);
		if(logger == null)
		{
			return "";
		}

		Level level = logger.getLevel();

		level = level == null ? logger.getEffectiveLevel() : level;

		return level == null ? "null" : level.toString();
	}

	public boolean setLevel(String name, String level)
	{
		Logger logger = LogManager.getLogger(name);
		if(logger != null)
		{
			logger.setLevel(Level.toLevel(level));

			return true;
		}
		else
		{
			return false;
		}
	}

	public Enumeration<Logger> getLoggers()
	{
		LoggerRepository repo = LogManager.getLoggerRepository();
		@SuppressWarnings("unchecked")
		Enumeration<Logger> loggers = repo.getCurrentLoggers();

		return loggers;
	}
}
