package org.suren.littlebird;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

public class HomeLuancher
{

	/**
	 * @param args
	 * @throws AlreadyBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, RemoteException, AlreadyBoundException
	{
		ResourceLoader loader = new ResourceLoader();
		
		loader.discover();
	}

}
