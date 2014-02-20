package org.suren.littlebird.net;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Scp")
public class HomeScp extends UnicastRemoteObject implements ScpRmi
{
	public static Set<File> tmpPath = new HashSet<File>();
	
	public HomeScp() throws RemoteException
	{
		super();
	}

	private static final long	serialVersionUID	= 5194088841214703659L;

	@Override
	public boolean push(String... path)
	{
		if(path == null)
		{
			return false;
		}
		
		for(String item : path)
		{
			File file = new File(item);

			if(file.isFile() && file.exists())
			{
				tmpPath.add(file);
			}
		}
		
		return true;
	}

	@Override
	public boolean push(String address, String path) throws Exception
	{
		return false;
	}

	@Override
	public boolean push(User user, boolean async) throws Exception
	{
		return false;
	}

	@Override
	public boolean push(User user) throws Exception
	{
		return push(user, true);
	}
}
