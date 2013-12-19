package org.suren.littlebird;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import org.suren.littlebird.log.ArchLogger;

public final class Mouse implements Runnable
{
	private static Mouse mouse = new Mouse();
	
	private Point point = new Point();
	private AtomicBoolean done = new AtomicBoolean();
	private Robot robot = null;
	
	private ArchLogger logger = ArchLogger.getInstance();
	
	private final int MAX_CLICK_TIMES = 5;
	
	public enum ClickType
	{
		Left,
		LeftPress,
		LeftRelease,
		Middle,
		MiddlePress,
		MiddleRelease,
		Right,
		RightPress,
		RightRelease
	}
	
	private Mouse()
	{
	}
	
	public static Mouse getInstance()
	{
		return mouse;
	}
	
	public void move(int x, int y)
	{
		synchronized (point)
		{
			point.setLocation(x, y);
			
			point.notifyAll();
		}
	}
	
	public void moveBy(int x, int y)
	{
		synchronized (point)
		{
			point.setLocation(point.x + x, point.y + y);
			
			point.notifyAll();
		}
	}
	
	public void click(ClickType type, int times)
	{
		int mask = -1;
		times = clickLimit(times);
		int event = 0x0;
		
		switch(type)
		{
			case Left:
				mask = InputEvent.BUTTON1_MASK;
				event = 0x3;
				break;
			case LeftPress:
				mask = InputEvent.BUTTON1_MASK;
				event = 0x2;
				break;
			case LeftRelease:
				mask = InputEvent.BUTTON1_MASK;
				event = 0x1;
				break;
			case Middle:
				mask = InputEvent.BUTTON2_MASK;
				event = 0x3;
				break;
			case MiddlePress:
				mask = InputEvent.BUTTON2_MASK;
				event = 0x2;
				break;
			case MiddleRelease:
				mask = InputEvent.BUTTON2_MASK;
				event = 0x1;
				break;
			case Right:
				mask = InputEvent.BUTTON3_MASK;
				event = 0x3;
				break;
			case RightPress:
				mask = InputEvent.BUTTON3_MASK;
				event = 0x2;
				break;
			case RightRelease:
				mask = InputEvent.BUTTON3_MASK;
				event = 0x1;
				break;
		}
		
		if(mask == -1 || event == 0x0)
		{
			return;
		}
		
		for(int i = 0; i < times; i++)
		{
			if((event & 0x2) != 0)
			{
				robot.mousePress(mask);
			}
			
			if((event & 0x1) != 0)
			{
				robot.mouseRelease(mask);
			}
		}
	}
	
	private int clickLimit(int times)
	{
		if(times > MAX_CLICK_TIMES)
		{
			times = MAX_CLICK_TIMES;
		}
		
		if(times < 0)
		{
			times = 0;
		}
		
		return times;
	}
	
	@Override
	public void run()
	{
		try {
			synchronized (this)
			{
				if(robot == null)
				{
					robot = new Robot();
					done.set(false);
				}
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		logger.info("server started.");
		
		while(true)
		{
			synchronized (point)
			{
				try
				{
					point.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			if(done.get() || robot == null)
			{
				break;
			}
			
			robot.mouseMove(point.x, point.y);
		}
	}
	
	public void done()
	{
		move(0, 0);
		
		synchronized (this)
		{
			robot = null;
		}
		
		done.set(true);
	}
}
