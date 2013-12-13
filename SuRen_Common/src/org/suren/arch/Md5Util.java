package org.suren.arch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util
{
	private final int MD5_LEN = 16;
	private final int MD5_STR_LEN = MD5_LEN * 2;
	
	private char hexDigits[] = new char[MD5_LEN];
	private MessageDigest md;
	
	public boolean init()
	{
		for(int i = 0; i < hexDigits.length; i++)
		{
			hexDigits[i] = Integer.toHexString(i).charAt(0);
		}
		
		try
		{
			md = MessageDigest.getInstance("md5");
		}
		catch (NoSuchAlgorithmException e)
		{
			return false;
		}
		
		return true;
	}
	
	public String getHash(String str)
	{
		initCheck();
		
		md.reset();
		md.update(str.getBytes());
		
		return toHexStr(md.digest());
	}
	
	public String getHash(File file)
	{
		initCheck();
		
		if(file == null)
		{
			return null;
		}
		
		FileInputStream fileInStream = null;
		try
		{
			fileInStream = new FileInputStream(file);
			
			byte[] buffer = new byte[1024];
			int len = -1;
			
			md.reset();
			
			while((len = fileInStream.read(buffer)) != -1)
			{
				md.update(buffer, 0, len);
			}
			
			return toHexStr(md.digest());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fileInStream != null)
			{
				try
				{
					fileInStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	private void initCheck()
	{
		if(ParamUtil.hasEmpty(md))
		{
			throw new NoInitException();
		}
	}
	
	private String toHexStr(byte[] buffer)
	{
		if(buffer == null)
		{
			return null;
		}
		
		char[] result = new char[MD5_STR_LEN];
		
		for(int i = 0, j = 0; i < MD5_LEN; i++)
		{
			byte item = buffer[i];
			
			result[j++] = hexDigits[item >>> 4 & 0xf];
			result[j++] = hexDigits[item & 0xf];
		}
		
		return new String(result);
	}
}
