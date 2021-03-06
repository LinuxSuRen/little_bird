package org.suren.littlebird.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.suren.littlebird.io.IoUtil;

public class SettingUtil<T>
{
	public boolean save(T type, OutputStream stream, Class<?> ... clazz)
	{
		JAXBContext context = getContext(clazz);
		if(context == null || type == null || stream == null)
		{
			return false;
		}
		
		try
		{
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(type, stream);
			
			return true;
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean save(T type, String path, Class<?> ... clazz)
	{
		File file = null;
		FileOutputStream outStream = null;
		if(path == null)
		{
			return false;
		}
		
		if(!(file = new File(path)).isFile())
		{
			try
			{
				if(!file.createNewFile())
				{
					return false;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				
				return false;
			}
		}
		
		try
		{
			outStream = new FileOutputStream(file);
			
			return save(type, outStream, clazz);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IoUtil.closeIo(outStream);
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public T load(InputStream stream, Class<?> ... clazz)
	{
		JAXBContext context = getContext(clazz);
		if(context == null || stream == null)
		{
			return null;
		}
		
		try
		{
			if(stream.available() <= 0)
			{
				return null;
			}
			
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			Object result = unmarshaller.unmarshal(stream);
			if(result.getClass().equals(clazz[0]))
			{
				return (T) result;
			}
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
			
		return null;
	}
	
	public T load(String path, Class<?> ... clazz)
	{
		File file;
		FileInputStream inStream = null;
		if(path == null || !(file = new File(path)).isFile())
		{
			return null;
		}
		
		try
		{
			inStream = new FileInputStream(file);
			
			return load(inStream, clazz);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IoUtil.closeIo(inStream);
		}
		
		return null;
	}
	
	private JAXBContext getContext(Class<?> ... clazz)
	{
		try
		{
			return JAXBContext.newInstance(clazz);
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
