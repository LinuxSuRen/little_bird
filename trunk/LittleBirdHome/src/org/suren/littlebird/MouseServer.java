package org.suren.littlebird;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.suren.littlebird.Mouse.ClickType;

public class MouseServer extends SimpleServer
{
	private static final String ACTION_START = "start";
	private static final String ACTION_MOVE = "move-";
	private static final String ACTION_MOVE_BY = "move_by-";
	private static final String ACTION_CLICK = "click-";
	private static final String ACTION_CLICK_PRESS = "click_press-";
	private static final String ACTION_CLICK_RELEASE = "click_release-";
	private static final String ACTION_DONE = "done";
	
	private static final String MSG_ERR = "error";
	private static final String MSG_OK = "ok";

	@Override
	public boolean init(int port)
	{
		if(!super.init(port))
		{
			return false;
		}
		
		int capacity = 1;
		serviceQueue = new ArrayBlockingQueue<ClientInfo>(capacity);
		
		logger.info("mouse server init success.");
		
		return true;
	}

	@Override
	public void run()
	{
		if(!isInited())
		{
			logger.error("mouse server no inited.");
			
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
				
				logger.info("get ticket : " + info);
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
	
	private void execute(Socket client)
	{
		Mouse mouseServer = Mouse.getInstance();
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			in = client.getInputStream();
			out = client.getOutputStream();
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
			
			StringBuilder builder = new StringBuilder();
			byte[] buffer = new byte[1024];
			int len = -1;
			
			try
			{
				len = in.read(buffer);
			}
			catch (IOException e1)
			{
				break;
			}
			
			if(len > 0)
			{
				builder.append(new String(buffer, 0, len));
			}
			else
			{
				break;
			}
			
			if(builder.toString().equals(ACTION_START))
			{
				new Thread(mouseServer).start();
			}
			else if(builder.toString().startsWith(ACTION_MOVE))
			{
				String action = builder.toString();
				String locStr = action.substring(ACTION_MOVE.length());
				String[] locStrArr = locStr.split(",");
				
				if(locStrArr != null && locStrArr.length == 2)
				{
					try
					{
						int x = Integer.parseInt(locStrArr[0]);
						int y = Integer.parseInt(locStrArr[1]);
						
						mouseServer.move(x, y);
					}
					catch(NumberFormatException e)
					{
					}
				}
			}
			else if(builder.toString().startsWith(ACTION_MOVE_BY))
			{
				String action = builder.toString();
				String locStr = action.substring(ACTION_MOVE_BY.length());
				String[] locStrArr = locStr.split(",");
				
				if(locStrArr != null && locStrArr.length == 2)
				{
					try
					{
						int x = Integer.parseInt(locStrArr[0]);
						int y = Integer.parseInt(locStrArr[1]);
						
						mouseServer.moveBy(x, y);
					}
					catch(NumberFormatException e)
					{
					}
				}
			}
			else if(builder.toString().startsWith(ACTION_CLICK))
			{
				String action = builder.toString();
				String keyStr = action.substring(ACTION_CLICK.length());
				String[] keyStrArr = keyStr.split(",");
				
				if(keyStrArr != null && keyStrArr.length == 2)
				{
					try
					{
						int keyCode = Integer.parseInt(keyStrArr[0]);
						int times = Integer.parseInt(keyStrArr[1]);
						Mouse.ClickType type = null;
						
						switch(keyCode)
						{
							case 0:
								type = ClickType.Left;
								break;
							case 1:
								type = ClickType.Middle;
								break;
							case 2:
								type = ClickType.Right;
								break;
						}
						
						if(type != null)
						{
							mouseServer.click(type, times);
						}
					}
					catch(NumberFormatException e)
					{
					}
				}
			}
			else if(builder.toString().startsWith(ACTION_CLICK_PRESS))
			{
				String action = builder.toString();
				String keyStr = action.substring(ACTION_CLICK_PRESS.length());
				
				try
				{
					int keyCode = Integer.parseInt(keyStr);
					Mouse.ClickType type = null;
					
					switch(keyCode)
					{
						case 0:
							type = ClickType.LeftPress;
							break;
						case 1:
							type = ClickType.MiddlePress;
							break;
						case 2:
							type = ClickType.RightPress;
							break;
					}
					
					if(type != null)
					{
						mouseServer.click(type, 1);
					}
				}
				catch(NumberFormatException e)
				{
				}
			}
			else if(builder.toString().startsWith(ACTION_CLICK_RELEASE))
			{
				String action = builder.toString();
				String keyStr = action.substring(ACTION_CLICK_RELEASE.length());
				
				try
				{
					int keyCode = Integer.parseInt(keyStr);
					ClickType type = null;
					
					switch(keyCode)
					{
						case 0:
							type = ClickType.LeftRelease;
							break;
						case 1:
							type = ClickType.MiddleRelease;
							break;
						case 2:
							type = ClickType.RightRelease;
							break;
					}
					
					if(type != null)
					{
						mouseServer.click(type, 1);
					}
				}
				catch(NumberFormatException e)
				{
				}
			}
			else if(builder.toString().startsWith(ACTION_DONE))
			{
				mouseServer.done();
			}
			else
			{
				try {
					out.write(MSG_ERR.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			
			try {
				out.write(MSG_OK.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		logger.info("client : " + client + " closed.");
	}
}
