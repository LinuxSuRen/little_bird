package org.suren.littlebird.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

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

	public boolean addBridge(String name, String host, int port)
	{
		LoggerRepository repo = LogManager.getLoggerRepository();

		Logger logger = repo.getLogger(name);
		if(logger == null)
		{
			return false;
		}

		Appender appender = logger.getAppender(host + port);
		if(appender != null)
		{
			return true;
		}

		appender = new SocketAppender(host, port);
		appender.setName(host + port);
		logger.addAppender(appender);

		SocketAppender abc = (SocketAppender) appender;
		abc.setReconnectionDelay(2000);
		LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "add socket aappend success.", null);
		abc.doAppend(event);

		return true;
	}

	public boolean removeBridge(String name, String host, int port)
	{
		return false;
	}

	public boolean clearBridges(String name)
	{
		return false;
	}

	public boolean clearBridges()
	{
		return false;
	}

	public List<String> getBridges(String name)
	{
		return null;
	}

	public List<String> getBridges()
	{
		return null;
	}
}
