package org.suren.littlebird;

public class ClientInfo
{
	private boolean empty;
	
	private String address;
	private int port;
	private long connectedTime;
	
	public ClientInfo()
	{
		empty = true;
	}
	
	public ClientInfo(String address, int port, long connectedTime)
	{
		this.address = address;
		this.port = port;
		this.connectedTime = connectedTime;
		
		empty = false;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public long getConnectedTime()
	{
		return connectedTime;
	}

	public void setConnectedTime(long connectedTime)
	{
		this.connectedTime = connectedTime;
	}

	public boolean isEmpty()
	{
		return empty;
	}
}
