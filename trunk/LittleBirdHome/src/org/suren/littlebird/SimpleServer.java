package org.suren.littlebird;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SimpleServer implements ArchServer
{
	protected AtomicReference<ServerSocket> serverRef = new AtomicReference<ServerSocket>(null);
	protected ArrayBlockingQueue<Long> serviceQueue = null;
	protected ExecutorService servicePool = null;

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
	
	@Override
	public boolean stop()
	{
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
				e.printStackTrace();
				
				return false;
			}
			
			serverRef.set(null);
		}
		
		servicePool.shutdown();
		
		return true;
	}
}
