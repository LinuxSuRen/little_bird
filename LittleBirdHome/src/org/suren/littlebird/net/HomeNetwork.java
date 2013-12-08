package org.suren.littlebird.net;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Network")
public class HomeNetwork extends UnicastRemoteObject implements Network
{

	public HomeNetwork() throws RemoteException
	{
		super();
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6755390188259019032L;

	@Override
	public String[] nicList() throws RemoteException
	{
		Enumeration<NetworkInterface> networkInters = null;
		
		try
		{
			networkInters = NetworkInterface.getNetworkInterfaces();
			
			if(networkInters == null)
			{
				return null;
			}
			
			while(networkInters.hasMoreElements())
			{
				NetworkInterface networkInter = networkInters.nextElement();
				
				if(networkInter.isLoopback() || networkInter.isVirtual())
				{
					continue;
				}
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
