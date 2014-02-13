package org.suren.littlebird.concurrent;

import java.util.concurrent.ThreadFactory;

public class LimitThreadFactory implements ThreadFactory
{
	private int limit;
	private ThreadGroup group;

	@SuppressWarnings("unused")
	private LimitThreadFactory()
	{
	}

	public LimitThreadFactory(int limit)
	{
		if(limit < 1)
		{
			limit = 1;
		}

		this.limit = limit;

		group = new ThreadGroup("LimitThreadGroup");
	}

	@Override
	public Thread newThread(Runnable runnable)
	{
		int count = group.activeCount();
		if(count >= limit)
		{
			return null;
		}

		String name = "LimitThread-" + count;

		Thread thread = new Thread(group, runnable, name);

		return thread;
	}
}
