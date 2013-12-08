package org.suren.littlebird.shell;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Shell")
public class HomeShell extends UnicastRemoteObject implements Shell
{

	public HomeShell() throws RemoteException
	{
		super();
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2832685704024714355L;

	@Override
	public boolean run(String cmd) throws RemoteException
	{
		return false;
	}

	@Override
	public String getEnv(String name) throws RemoteException
	{
		return System.getenv(name);
	}

}
