package org.suren.littlebird;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientInfo
{
	private Socket socket;
	private String address;
	private int port;
	private long connectedTime;
	
	public ClientInfo()
	{
	}
	
	public ClientInfo(String address, int port, long connectedTime)
	{
		this.address = address;
		this.port = port;
		this.connectedTime = connectedTime;
	}
	
	public boolean tearDown()
	{
		if(socket != null)
		{
			try
			{
				System.out.println(socket + "closed.");
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
		if(socket == null)
		{
			return;
		}
		
		SocketAddress socketAddr = socket.getRemoteSocketAddress();
		if(socketAddr instanceof InetSocketAddress)
		{
			InetSocketAddress inetSocketaddr = (InetSocketAddress)
					socketAddr;
			
			this.setAddress(inetSocketaddr.getHostName());
			this.setPort(inetSocketaddr.getPort());
		}
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
}
