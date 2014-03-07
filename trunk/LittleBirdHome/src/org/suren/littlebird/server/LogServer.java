package org.suren.littlebird.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.spi.LoggingEvent;
import org.suren.littlebird.ArchServerListener;
import org.suren.littlebird.ClientInfo;

public class LogServer extends SimpleServer
{
	private LogServerListener logServerListener;

	@Override
	public boolean init(int port)
	{
		if(!super.init(port))
		{
			return false;
		}
		
		int capacity = 13;
		serviceQueue = new ArrayBlockingQueue<ClientInfo>(capacity);
		
		if(logServerListener == null)
		{
			setListener(null);
		}
		
		logServerListener.printMessage("mouse server init success.");
		
		return true;
	}

	@Override
	public void run()
	{
		if(!isInited())
		{
			logServerListener.printMessage("log server no inited.");
			
			return;
		}
		
		while(true)
		{
			ServerSocket server = serverRef.get();
			if(server == null || server.isClosed() || !server.isBound())
			{
				break;
			}
			
			ClientInfo info = new ClientInfo();
			try
			{
				if(!serviceQueue.offer(info, 5000, TimeUnit.MILLISECONDS))
				{
					continue;
				}
				
				logServerListener.printMessage("get ticket : " + info);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
				break;
			}
			
			Socket client;
			try
			{
				client = server.accept();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				serviceQueue.remove(info);
				
				continue;
			}
			
			info.setSocket(client);
			process(info);
		}
	}

	private void process(final ClientInfo info)
	{
		servicePool.execute(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					if(serverListener != null)
					{
						serverListener.onLine(info);
					}
					
					execute(info.getSocket());
				}
				finally
				{
					serviceQueue.remove(info);
					
					if(serverListener != null)
					{
						serverListener.offLine(info);
					}
				}
			}
		});
	}

	protected void execute(Socket client)
	{
		ObjectInputStream reader = null;
		
		try
		{
			reader = new ObjectInputStream(client.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			return;
		}
		
		while(true)
		{
			if(client.isClosed() || client.isInputShutdown()
					|| client.isOutputShutdown())
			{
				break;
			}
			
			LoggingEvent source = null;
			try
			{
				Object obj = reader.readObject();
				
				if(!(obj instanceof LoggingEvent))
				{
					logServerListener.printMessage("not correct object.");
					
					continue;
				}
				
				source = (LoggingEvent) obj;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			
			if(source == null)
			{
				continue;
			}
			
			logServerListener.printMessage(source.getMessage().toString());
			System.out.println(source.getMessage());
		}
	}

	@Override
	public void setListener(ArchServerListener listener)
	{
		if(listener != null && listener instanceof LogServerListener)
		{
			logServerListener = (LogServerListener) listener;
		}
		else
		{
			logServerListener = new LogServerListener()
			{
				
				@Override
				public void onLine(ClientInfo clientInfo)
				{
				}
				
				@Override
				public void offLine(ClientInfo clientInfo)
				{
				}
				
				@Override
				public void printMessage(CharSequence msg)
				{
				}
			};
		}
	}
}
