package org.suren.littlebird.io;

import java.io.Closeable;
import java.io.IOException;

public class IoUtil
{
	public static boolean closeIo(Closeable closeable)
	{
		if(closeable == null)
		{
			return true;
		}
		
		try
		{
			closeable.close();
			
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
