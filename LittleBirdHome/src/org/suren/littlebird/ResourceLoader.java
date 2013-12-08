package org.suren.littlebird;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.suren.littlebird.annotation.Publish;

public class ResourceLoader
{
	private Registry registry;
	private ExecutorService service = Executors.newCachedThreadPool();
	private ArrayBlockingQueue<Class<?>> classQueue =
			new ArrayBlockingQueue<Class<?>>(100);
	private String prefix;
	private boolean done = false;
	
	public void discover(int port) throws RemoteException
	{
		String name = this.getClass().getPackage().getName();
		prefix = name.replace(".", File.separator);
		
		registry = LocateRegistry.createRegistry(port);
		
		service.submit(findClassTask);
		service.submit(findPublishTask);
	}
	
	public void discover() throws RemoteException
	{
		discover(8989);
	}
	
	private Callable<String> findClassTask = new Callable<String>()
	{

		@Override
		public String call() throws Exception
		{
			Enumeration<URL> res =
					ClassLoader.getSystemResources(prefix);
			
			while(res.hasMoreElements())
			{
				URL url = res.nextElement();
				if(!"file".equals(url.getProtocol()))
				{
					continue;
				}
				
				String path = URLDecoder.decode(url.getPath(), "utf-8");
				File file = new File(path);
				
				filter(file);
			}
			
			synchronized (service)
			{
				done = true;
			}
			
			return "";
		}
	};
	
	private void filter(File file)
	{
		if(file == null)
		{
			return;
		}
		
		if(file.isDirectory())
		{
			File[] subFiles = file.listFiles();
			
			for(File sub : subFiles)
			{
				filter(sub);
			}
		}
		else if(file.isFile())
		{
			String absPath = file.getAbsolutePath();
			int index = absPath.indexOf(prefix);
			
			if(index == -1)
			{
				return;
			}
			
			String clsName = absPath.substring(index);
			clsName = clsName.replace(File.separatorChar, '.');
			clsName = clsName.replace(".class", "");
			
			tryRecognize(clsName);
		}
	}
	
	private boolean tryRecognize(String className)
	{
		try
		{
			Class<?> cls = Class.forName(className);
			Annotation annotation = cls.getAnnotation(Publish.class);
			
			if(annotation == null)
			{
				return false;
			}
			
			classQueue.put(cls);
			System.out.println("discover : " + cls);
			
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
		catch (InterruptedException e)
		{
			return false;
		}
	}
	
	private Callable<String> findPublishTask = new Callable<String>()
	{

		@Override
		public String call() throws Exception
		{
			while(true)
			{
				synchronized (service)
				{
					if(done && classQueue.size() == 0)
					{
						break;
					}
				}
				
				Class<?> cls = classQueue.take();
				
				Publish publish = cls.getAnnotation(Publish.class);
				Object obj = cls.newInstance();
				if(obj instanceof Remote && publish != null)
				{
					Remote remoteObj = (Remote)obj;
					
					registry.bind(publish.name(), remoteObj);
					
					System.out.println(publish.name() + " is published.");
				}
			}
			
			return null;
		}
	};
}
