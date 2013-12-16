package org.suren.littlebird.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MouseClientTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		if(args == null || args.length != 3)
		{
			return;
		}
		
		String action = args[0];
		String xStr = args[1];
		String yStr = args[2];
		
		int x = -1;
		int y = -1;
		
		try
		{
			x = Integer.parseInt(xStr);
			y = Integer.parseInt(yStr);
		}
		catch(NumberFormatException e)
		{
		}
		
		if(x == -1 || y == -1)
		{
			return;
		}
		
		Socket socket = new Socket("10.0.32.3", 8989);
		OutputStream out = socket.getOutputStream();
		
		if("start".equals(action))
		{
			out.write("start".getBytes());
		}
		else if("move".equals(action))
		{
			out.write(("move" + x + "," + y).getBytes());
		}
		else if("done".equals(action))
		{
			out.write("done".getBytes());
		}
		
		out.close();
		socket.close();
	}
}
