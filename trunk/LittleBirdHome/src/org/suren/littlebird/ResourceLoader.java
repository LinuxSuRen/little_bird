package org.suren.littlebird;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.suren.littlebird.log.ArchLogger;
import org.suren.littlebird.log.ConsoleAppender;
import org.suren.littlebird.log.DefaultAppender;

public class ResourceLoader
{
	private ExecutorService service = Executors.newCachedThreadPool();
	private ArrayBlockingQueue<Class<?>> classQueue =
			new ArrayBlockingQueue<Class<?>>(100);
	private AtomicReference<HashMap<Class<?>, ArrayList<Class<?>>>> result =
			new AtomicReference<HashMap<Class<?>, ArrayList<Class<?>>>>(null);
	private static AtomicReference<ResourceLoader> loaderRef =
			new AtomicReference<ResourceLoader>(null);
	private String prefix;
	private AtomicBoolean done = new AtomicBoolean(false);
	private ArchLogger logger = ArchLogger.getInstance();
	private DefaultAppender appender = new ConsoleAppender();
	
	private ResourceLoader()
	{
		appender.addFilter(this.getClass().getName());
		
		logger.putAppender(appender);
	}
	
	public static ResourceLoader getInstance()
	{
		ResourceLoader loader = loaderRef.get();
		if(loader == null)
		{
			loaderRef.set(new ResourceLoader());
			loader = loaderRef.get();
		}
		
		return loader;
	}
	
	public void discover(final Class<?> ... annos)
	{
		if(annos == null || annos.length == 0)
		{
			return;
		}
		
		String name = this.getClass().getPackage().getName();
		prefix = name.replace(".", "/");
		logger.debug("discover begin. prefix : " + prefix);
		
		result.set(new HashMap<Class<?>, ArrayList<Class<?>>>()); 
		
		service.submit(findClassTask);
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				appender.addFilter(this.getClass().getName());
				
				while(true)
				{
					synchronized (service)
					{
						if(done.get() && classQueue.size() == 0)
						{
							break;
						}
					}
					
					Class<?> cls = null;
					try
					{
						cls = classQueue.take();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					
					if(cls == null)
					{
						continue;
					}
					
					for(Class anno : annos)
					{
						Object obj = cls.getAnnotation(anno);
						
						if(obj == null)
						{
							continue;
						}
						
						ArrayList<Class<?>> list = result.get().get(anno);
						if(list == null)
						{
							list = new ArrayList<Class<?>>();
							result.get().put(anno, list);
						}
						
						list.add(cls);
						logger.debug("discover annotation : " + obj);
						
						break;
					}
				}
			}
		});
	}
	
	private Runnable findClassTask = new Runnable()
	{
		@Override
		public void run()
		{
			appender.addFilter(this.getClass().getName());
			
			Enumeration<URL> res = null;
			try
			{
				res = ClassLoader.getSystemClassLoader().getResources(prefix);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(res != null)
			{
				try
				{
					while(res.hasMoreElements())
					{
						URL url = res.nextElement();
						logger.debug("url:" + url);
						
						if("file".equals(url.getProtocol()))
						{
							String path = URLDecoder.decode(url.getPath(), "utf-8");
							File file = new File(path);
							
							logger.debug("found file : " + path);
							filter(file);
						}
						else if("jar".equals(url.getProtocol()))
						{
							int spIndex = url.getPath().indexOf("!");
							if(spIndex == -1)
							{
								continue;
							}
							
							String path = URLDecoder.decode(
									url.getPath().substring(0, spIndex), "utf-8");
							File jarFile = new File(path.substring(5));
							jarFilter(jarFile);
						}
					}
				}
				catch(UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			
			synchronized (service)
			{
				done.set(true);
			}
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
			absPath = absPath.replace('\\', '/');
			int index = absPath.indexOf(prefix);
			
			if(index == -1)
			{
				return;
			}
			
			String clsName = absPath.substring(index);
			clsName = clsName.replace('/', '.');
			clsName = clsName.replace(".class", "");
			
			tryRecognize(clsName);
		}
	}
	
	private void jarFilter(File file)
	{
		if(file == null)
		{
			return;
		}
		
		if(!file.isFile())
		{
			logger.error("jar file not found: " + file.getAbsolutePath());
			
			return;
		}
		
		try
		{
			JarFile jarFile = new JarFile(file);
			
			Enumeration<JarEntry> entries = jarFile.entries();
			if(entries == null)
			{
				return;
			}
			
			while(entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				
				if(entry.isDirectory())
				{
					continue;
				}
				
				String path = entry.getName();
				if(path.startsWith(prefix))
				{
					path = path.replace(".class", "");
					path = path.replace("\\", ".");
					path = path.replace("/", ".");
					
					tryRecognize(path);
				}
			}
		}
		catch (IOException e)
		{
			logger.error("jarFile access error." + file.getAbsolutePath());
			return;
		}
	}
	
	private boolean tryRecognize(String className)
	{
		try
		{
			Class<?> cls = Class.forName(className);
			Annotation[] annotations = cls.getAnnotations();
			
			if(annotations == null)
			{
				return false;
			}
			
			classQueue.put(cls);
			
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
	
	public boolean isFinished()
	{
		return done.get();
	}
	
	public List<Class<?>> getResult(Class<?> type)
	{
		return result.get().get(type);
	}
}