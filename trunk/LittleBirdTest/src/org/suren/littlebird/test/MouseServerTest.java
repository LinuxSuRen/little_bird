package org.suren.littlebird.test;

import org.suren.littlebird.Mouse;
import org.suren.littlebird.MouseServer;

public class MouseServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final Mouse server = Mouse.getInstance();
		
		new Thread(server)
		{
		}.start();
		
		new Thread()
		{
			public void run()
			{
				int x = 1;
				int y = 1;
				while(true)
				{
					server.move(++x, ++y);
					
					try
					{
						Thread.sleep(6);
					}
					catch(InterruptedException e)
					{
					}
				}
			}
		}.start();
		
		new Thread()
		{
			public void run()
			{
				try
				{
					Thread.sleep(9000);
				}
				catch(InterruptedException e)
				{
				}
				
				server.done();
			}
		}.start();
	}

}
