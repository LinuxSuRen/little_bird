package org.suren.littlebird;

import java.io.IOException;
import java.net.Socket;

public class ClientInfo
{
	private boolean empty;
	
	private Socket socket;
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
	
	public boolean tearDown()
	{
		if(socket != null)
		{
			try
			{
				socket.close();
				
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return false;
	}

	public Socket getSocket()
	{
		return socket;
	}

	public void setSocket(Socket socket)
	{
		this.socket = socket;
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
