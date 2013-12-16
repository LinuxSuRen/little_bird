package org.suren.littlebird.test;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

public class RobotTest {

	/**
	 * @param args
	 * @throws AWTException 
	 */
	public static void main(String[] args) throws AWTException {
		Robot robot = new Robot();
		
		int x = 0;
		int y = 0;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		
		while(true)
		{
			robot.mouseMove(x, y);
			
			if(x >= width || y >= height)
			{
				break;
			}
			else
			{
				++x;
				++y;
			}
		
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
			}
		}
	}

}
