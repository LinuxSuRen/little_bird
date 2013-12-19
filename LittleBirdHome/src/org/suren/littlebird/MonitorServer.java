package org.suren.littlebird;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class MonitorServer extends SimpleServer
{
	private static final String ACTION_CAPTURE = "capture-";
	private static final String ACTION_CAPTURE_BY = "capture_by-";
	private static final String ACTION_CAPTURE_AUTO = "capture_auto-";
	
	private static final String[] ACTION_CAPTURE_ARRAY = {ACTION_CAPTURE, ACTION_CAPTURE_BY,
		ACTION_CAPTURE_AUTO};
	
	private ThreadLocal<Point> localPoint = new ThreadLocal<Point>();
	private ThreadLocal<Dimension> localDimension = new ThreadLocal<Dimension>();
	
	private float quality = 0.1f;

	@Override
	public boolean init(int port)
	{
		if(!super.init(port))
		{
			return false;
		}
		
		int capacity = 20;
		serviceQueue = new ArrayBlockingQueue<ClientInfo>(capacity);
		
		return true;
	}

	@Override
	public void run()
	{
		if(!isInited())
		{
			logger.error("server no inited.");
			
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
			monitorDispatch(info);
		}
	}
	
	private void monitorDispatch(final ClientInfo info)
	{
		servicePool.execute(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					execute(info.getSocket());
				}
				finally
				{
					serviceQueue.remove(info);
				}
			}
		});
	}
	
	private void execute(Socket client)
	{
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			in = client.getInputStream();
			out = client.getOutputStream();
		}
		catch(IOException e)
		{
			return;
		}
		
		while(true)
		{
			if(client.isClosed() || client.isInputShutdown()
					|| client.isOutputShutdown())
			{
				break;
			}
			
			byte[] buffer = new byte[1024];
			int len = -1;
			
			try
			{
				len = in.read(buffer);
			}
			catch(IOException e)
			{
				break;
			}
			
			if(len == -1)
			{
				break;
			}
			
			String cmd = new String(buffer, 0, len);
			if(cmd.startsWith(ACTION_CAPTURE) || cmd.startsWith(ACTION_CAPTURE_BY)
					|| cmd.startsWith(ACTION_CAPTURE_AUTO))
			{
				ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(4000);
				
				if(capture(cmd, byteArrayOut))
				{
					try
					{
						byte[] byteBuffer = byteArrayOut.toByteArray();
						
						logger.debug(byteBuffer.length);
						
						new DataOutputStream(out).writeInt(byteBuffer.length);
						out.write(byteBuffer);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						break;
					}
				}
				else
				{
					logger.error("error capture.");
				}
				
				continue;
			}
			else
			{
				try {
					new DataOutputStream(out).writeInt(1);
					out.write(0);
				} catch (IOException e) {
					logger.error("invalid cmd.");
					e.printStackTrace();
				}
			}
		}
	}

	private boolean capture(String cmd, ByteArrayOutputStream byteArrayOut)
	{
		Rectangle rectangle;
		if((rectangle = targetRectangleParse(cmd)) == null)
		{
			return false;
		}
		
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
			return false;
		}
		
		BufferedImage bufferedImage = robot.createScreenCapture(rectangle);

		JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(bufferedImage);
		param.setQuality(getQuality(), true);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayOut);
		
		Point relativePos = getRelativePos(rectangle);
		if(relativePos != null)
		{
			addCursor(bufferedImage, relativePos);
		}
		else
		{
			logger.error("unknow relative mouse point.");
		}
		
		try {
			encoder.encode(bufferedImage, param);
//			ImageIO.write(bufferedImage, "png", byteArrayOut);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		logger.debug("size : " + byteArrayOut.toByteArray().length + "\t"
				+ rectangle.getLocation().x + "-" + rectangle.getLocation().y
				+ rectangle.getWidth() + "-" + rectangle.getHeight());
		
		return true;
	}
	
	private Point getRelativePos(Rectangle rectangle)
	{
		Point point = null;
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		
		if(rectangle == null || pointerInfo == null)
		{
			return point;
		}
		
		point = new Point(pointerInfo.getLocation().x - rectangle.x,
				pointerInfo.getLocation().y - rectangle.y);
		
		return point;
	}
	
	private void addCursor(BufferedImage image, Point point)
	{
		if(image == null)
		{
			return;
		}
		
		Graphics2D g = image.createGraphics();
		g.setColor(Color.RED);
		g.drawString("R", point.x, point.y);
		g.dispose();
	}
	
	private Rectangle targetRectangleParse(String cmd)
	{
		Rectangle rectangle = null;
		if(cmd == null)
		{
			return rectangle;
		}
		
		int info[] = new int[4];
		for(String item : ACTION_CAPTURE_ARRAY)
		{
			if(pointParse(cmd, item, info))
			{
				rectangle = new Rectangle();
				
				break;
			}
		}
		
		if(rectangle == null)
		{
			return rectangle;
		}
		
		rectangle.width = info[0];
		rectangle.height = info[1];
		
		if(cmd.indexOf(ACTION_CAPTURE_AUTO) == 0)
		{
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			
			if(pointerInfo != null)
			{
				localPoint.set(pointerInfo.getLocation());
			}
			else
			{
				return null;
			}
		}
		else if(cmd.indexOf(ACTION_CAPTURE) == 0)
		{
			localPoint.get().x = info[2];
			localPoint.get().y = info[3];
		}
		else if(cmd.indexOf(ACTION_CAPTURE_BY) == 0)
		{
			localPoint.get().x += info[2];
			localPoint.get().y += info[3];
		}
		else 
		{
			return null;
		}
		
		rectangle.x = localPoint.get().x - rectangle.width / 2;
		rectangle.y = localPoint.get().y - rectangle.height / 2;
		
		boundaryLimit(rectangle);
		
		return rectangle;
	}
	
	private boolean pointParse(String cmd, String key, int info[])
	{
		if(cmd == null || key == null || info == null)
		{
			return false;
		}
		
		if(cmd.indexOf(key) == 0)
		{
			String tmpCmd = cmd.substring(key.length());
			String[] tmpInfo = tmpCmd.split(",");
			
			if(tmpInfo != null && tmpInfo.length == info.length)
			{
				try
				{
					for(int i = 0; i < tmpInfo.length; i++)
					{
						info[i] = Integer.parseInt(tmpInfo[i]);
					}
				}
				catch(NumberFormatException e)
				{
					return false;
				}
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void boundaryLimit(Rectangle rectangle)
	{
		if(rectangle == null)
		{
			return;
		}
		
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		
		if(rectangle.width > dimension.width)
		{
			rectangle.width = dimension.width;
		}
		else if(rectangle.width <= 0)
		{
			Dimension lastRec = localDimension.get();
			
			if(lastRec != null)
			{
				rectangle.width = lastRec.width;
			}
			else
			{
				rectangle.width = 100;
			}
		}
		else
		{
			lastDimensionRem(rectangle.width, -1);
		}
		
		if(rectangle.height > dimension.height)
		{
			rectangle.height = dimension.height;
		}
		else if(rectangle.height <= 0)
		{
			Dimension lastRec = localDimension.get();
			
			if(lastRec != null)
			{
				rectangle.height = lastRec.height;
			}
			else
			{
				rectangle.height = 100;
			}
		}
		else
		{
			lastDimensionRem(-1, rectangle.height);
		}
		
		if(rectangle.x < 0)
		{
			rectangle.x = 0;
		}
		else if(rectangle.x + rectangle.width > dimension.width)
		{
			rectangle.x = dimension.width - rectangle.width;
		}
		
		if(rectangle.y < 0)
		{
			rectangle.y = 0;
		}
		else if(rectangle.y + rectangle.height > dimension.height)
		{
			rectangle.y = dimension.height - rectangle.height;
		}
	}
	
	private void lastDimensionRem(int width, int height)
	{
		Dimension dimension = localDimension.get();
		if(dimension == null)
		{
			dimension = new Dimension();
		}
		
		if(width > 0)
		{
			dimension.setSize(width, dimension.getHeight());
		}
		
		if(height > 0)
		{
			dimension.setSize(dimension.getWidth(), height);
		}
		
		localDimension.set(dimension);
	}

	public float getQuality()
	{
		return quality;
	}

	public void setQuality(float quality)
	{
		if(quality < 0 || quality > 1)
		{
			return;
		}
		
		this.quality = quality;
	}
}
