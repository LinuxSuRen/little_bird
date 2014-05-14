package org.suren.littlebird.net.ssh;

import com.jcraft.jsch.Channel;

public class JSchConnection
{
	private Channel channel;

	public Channel getChannel()
	{
		return channel;
	}

	public void setChannel(Channel channel)
	{
		this.channel = channel;
	}
}
