package org.suren.littlebird.net;

public class NetworkInfo
{
	private String host;
	private long time;
	private boolean reachable;

	public boolean isReachable()
	{
		return reachable;
	}

	public void setReachable(boolean reachable)
	{
		this.reachable = reachable;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}
}
