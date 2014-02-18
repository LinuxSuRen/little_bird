package org.suren.littlebird.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

import org.suren.littlebird.ResourceLoader;
import org.suren.littlebird.annotation.Publish;
import org.suren.littlebird.log.ConsoleAppender;

public class RmiServer implements ArchServer
{
	private ConsoleAppender appender = new ConsoleAppender();
	private int port = 6600;

	@Override
	public void run()
	{
		ResourceLoader loader = ResourceLoader.getInstance();
		appender.addFilter(this.getClass().getName());
		
		while(!loader.isFinished())
		{
		}
		
		List<Class<?>> result = loader.getResult(Publish.class);
		List<Class<?>> tmpResult = new ArrayList<Class<?>>();
		if(result == null || result.size() == 0)
		{
			return;
		}
		
		try
		{
			LocateRegistry.createRegistry(port);
			
			tmpResult.addAll(result);
			for(Class<?> cls : tmpResult)
			{
				try
				{
					Object service = cls.newInstance();
					if(!(service instanceof Remote))
					{
						continue;
					}
					
					Naming.rebind("rmi://127.0.0.1:" + port + "/" + cls.getSimpleName(), (Remote) service);
					
					System.out.println("publish service : " + cls.getName());
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (RemoteException e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public boolean init(int port)
	{
		return false;
	}

	@Override
	public boolean isInited()
	{
		return false;
	}

	@Override
	public boolean stop()
	{
		return false;
	}

}
