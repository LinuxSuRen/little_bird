package org.suren.littlebird.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.suren.littlebird.util.AppenderConvert;

public class DefaultLoggerServer implements LoggerServer
{
	private static final long	serialVersionUID	= 1L;

	private Logger logger = Logger.getLogger(DefaultLoggerServer.class);

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
		Logger targetLogger = getLogger(name);
		if(targetLogger == null)
		{
			logger.error("can not found logger : " + name);

			return false;
		}

		String appenderName = host + port;
		Appender appender = targetLogger.getAppender(appenderName);
		if(appender != null)
		{
			logger.debug(appenderName + " already exists.");

			return true;
		}

		appender = new SocketAppender(host, port);
		appender.setName(appenderName);
		targetLogger.addAppender(appender);

		SocketAppender socketAppender = (SocketAppender) appender;
		socketAppender.setReconnectionDelay(2000);

		LoggingEvent event = getSimpleEvent(logger,
				Level.DEBUG, "add socket append success.");
		socketAppender.doAppend(event);

		logger.info(appenderName + " added.");

		return true;
	}

	public boolean removeBridge(String name, String host, int port)
	{
		Logger targetLogger = getLogger(name);
		if(targetLogger == null)
		{
			logger.error("can not found logger : " + name);

			return false;
		}

		String appenderName = host + port;
		Appender appender = targetLogger.getAppender(appenderName);
		if(appender == null)
		{
			logger.error("can not found appender : " + appenderName);

			return false;
		}

		if(!(appender instanceof SocketAppender))
		{
			logger.warn(appenderName + " is not a socket appender.");
		}

		targetLogger.removeAppender(appender);

		logger.info("removed appender : " + appenderName);

		return true;
	}

	public int clearBridges(String name)
	{
		int count = 0;
		if(name == null)
		{
			return count;
		}

		Enumeration<Logger> loggers = getLoggers();
		if(loggers == null)
		{
			return count;
		}

		while(loggers.hasMoreElements())
		{
			Logger targetLogger = loggers.nextElement();
			Appender appender = targetLogger.getAppender(name);

			if(appender != null)
			{
				targetLogger.removeAppender(appender);

				count++;
			}
		}

		return count;
	}

	@SuppressWarnings("unchecked")
	public int clearBridges()
	{
		int count = 0;
		Enumeration<Logger> loggers = getLoggers();
		if(loggers == null)
		{
			return count;
		}

		while(loggers.hasMoreElements())
		{
			Logger targetLogger = loggers.nextElement();
			Enumeration<Appender> appenders = targetLogger.getAllAppenders();

			while(appenders.hasMoreElements())
			{
				targetLogger.removeAppender(appenders.nextElement());

				count++;
			}
		}

		return count;
	}

	@SuppressWarnings("unchecked")
	public List<String> getBridges(String name)
	{
		List<String> bridges = new ArrayList<String>();
		Logger targetLogger = getLogger(name);
		if(targetLogger == null)
		{
			logger.error("can not found target logger : " + name);

			return bridges;
		}

		Enumeration<Appender> appenders = targetLogger.getAllAppenders();
		while(appenders.hasMoreElements())
		{
			Appender appender = appenders.nextElement();

			bridges.add(appender.getName());
		}

		return bridges;
	}

	@SuppressWarnings("unchecked")
	public List<String> getBridges()
	{
		List<String> bridges = new ArrayList<String>();
		Enumeration<Logger> loggers = getLoggers();
		if(loggers == null)
		{
			return bridges;
		}

		while(loggers.hasMoreElements())
		{
			Logger targetLogger = loggers.nextElement();
			Enumeration<Appender> appenders = targetLogger.getAllAppenders();

			while(appenders.hasMoreElements())
			{
				bridges.add(appenders.nextElement().getName());
			}
		}

		return bridges;
	}

	private LoggingEvent getSimpleEvent(Logger logger, Level level, String msg)
	{
		LoggingEvent event = new LoggingEvent("", logger, level, msg, null);

		return event;
	}

	private Logger getLogger(String name)
	{
		LoggerRepository repo = LogManager.getLoggerRepository();
		Logger logger = repo.getLogger(name);

		return logger;
	}

	public List<Entry<String, String>> bridgeInfo(String loggerName, String bridgeName)
	{
		Logger targetLogger  = getLogger(loggerName);
		if(targetLogger == null)
		{
			return null;
		}

		Appender appender = targetLogger.getAppender(bridgeName);
		if(appender == null)
		{
			return null;
		}

		return AppenderConvert.toList(appender);
	}
}
