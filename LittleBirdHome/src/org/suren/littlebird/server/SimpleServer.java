package org.suren.littlebird.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.suren.littlebird.ArchServerListener;
import org.suren.littlebird.ClientInfo;
import org.suren.littlebird.log.ArchLogger;

public abstract class SimpleServer implements ArchServer
{
	protected AtomicReference<ServerSocket> serverRef = new AtomicReference<ServerSocket>(null);
	protected ArrayBlockingQueue<ClientInfo> serviceQueue = null;
	protected ExecutorService servicePool = null;
	protected ArchLogger logger = ArchLogger.getInstance();
	protected ArchServerListener serverListener = null;

	@Override
	public boolean init(int port)
	{
		SocketAddress bindAddress = new InetSocketAddress(port);
		ServerSocket server = null;
		try
		{
			server = new ServerSocket();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		try
		{
			server.bind(bindAddress);
			serverRef.set(server);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		servicePool = Executors.newCachedThreadPool();
		
		return true;
	}

	@Override
	public boolean isInited()
	{
		boolean result = false;
		
		if(serverRef.get() != null && servicePool != null && serviceQueue != null)
		{
			result = true;
		}
		
		return result;
	}
	
	public void setListener(ArchServerListener listener)
	{
		serverListener = listener;
	}
	
	public int idleResource()
	{
		if(serviceQueue == null)
		{
			return -1;
		}
	
		return serviceQueue.remainingCapacity();
	}
	
	public int busyResource()
	{
		if(serviceQueue == null)
		{
			return -1;
		}
		
		return serviceQueue.size();
	}
	
	public List<ClientInfo> clientList()
	{
		List<ClientInfo> clients =  new ArrayList<ClientInfo>();
		
		Iterator<ClientInfo> iterator = serviceQueue.iterator();
		while(iterator.hasNext())
		{
			ClientInfo info = iterator.next();
			if(info.getSocket() == null)
			{
				continue;
			}
			
			clients.add(info);
		}
		
		return clients;
	}
	
	@Override
	public boolean stop()
	{
		List<ClientInfo> clientList = clientList();
		System.out.println(Arrays.toString(clientList.toArray()));
		for(ClientInfo clientInfo : clientList)
		{
			clientInfo.tearDown();
		}
		
		serviceQueue.clear();
		
		ServerSocket server = serverRef.get();
		if(server != null)
		{
			try
			{
				server.close();
			}
			catch (IOException e)
			{
				logger.error("server close error:" + e.getMessage());
				
				return false;
			}
			
			serverRef.set(null);
		}
		
		servicePool.shutdown();
		logger.info("server stoped.");
		
		return true;
	}
}
