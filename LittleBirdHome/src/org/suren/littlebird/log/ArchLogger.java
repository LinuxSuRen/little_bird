package org.suren.littlebird.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ArchLogger
{
	private enum Level{DEBUG, INFO, WARN, ERROR}
	private List<ArchAppender> appenders = new ArrayList<ArchAppender>();
	private static AtomicReference<ArchLogger> logger =
			new AtomicReference<ArchLogger>(null);
	
	private ArchLogger(){}
	
	public static ArchLogger getInstance()
	{
		ArchLogger obj = logger.get();
		if(obj == null)
		{
			logger.set(new ArchLogger());
			obj = logger.get();
		}
		
		return obj;
	}

	public void debug(Object obj)
	{
		log(Level.DEBUG, obj != null ? obj.toString() : "null");
	}
	
	public void info(Object obj)
	{
		log(Level.INFO, obj != null ? obj.toString() : "null");
	}
	
	public void warn(Object obj)
	{
		log(Level.WARN, obj != null ? obj.toString() : "null");
	}
	
	public void error(Object obj)
	{
		log(Level.ERROR, obj != null ? obj.toString() : "null");
	}
	
	private void log(Level level, CharSequence charSeq)
	{
		if(charSeq == null)
		{
			return;
		}

		StackTraceElement stackTrace = getRecordTrace();
		for(ArchAppender appender : appenders)
		{
			appender.append(charSeq + "\n", stackTrace);
		}
	}
	
	private StackTraceElement getRecordTrace()
	{
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		if(stackTraces != null && stackTraces.length > 1)
		{
			for(int i = 1; i < stackTraces.length; i++)
			{
				if(!stackTraces[i].getClassName().equals(this.getClass().getName()))
				{
					return stackTraces[i];
				}
			}
		}
		
		return null;
	}

	public void putAppender(ArchAppender appender)
	{
		if(appender != null)
		{
			synchronized (appenders)
			{
				appenders.add(appender);
			}
		}
	}

	public List<ArchAppender> getAppenders()
	{
		return appenders;
	}
}
