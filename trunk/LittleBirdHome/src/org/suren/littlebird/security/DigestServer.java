package org.suren.littlebird.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.suren.littlebird.concurrent.LimitThreadFactory;

public class DigestServer
{
	private static DigestServer server = new DigestServer();
	
	private boolean inited;
	private AtomicBoolean runnable = new AtomicBoolean(true);
	private ExecutorService service;
	private ArrayBlockingQueue<DigestTask> taskQueue;
	
	private DigestServer(){}
	
	public static DigestServer getInstance()
	{
		return server;
	}
	
	public void init(int limit, int capacity)
	{
		if(service != null)
		{
			return;
		}
		
		LimitThreadFactory threadFactory = new LimitThreadFactory(limit);
		
		service = Executors.newCachedThreadPool(threadFactory);
		
		taskQueue = new ArrayBlockingQueue<DigestTask>(capacity);
		
		setInited(true);
	}
	
	public boolean start()
	{
		if(!isInited())
		{
			return false;
		}

		service.submit(new Runnable()
		{
			
			@Override
			public void run()
			{
				while(runnable.get())
				{
					try
					{
						DigestTask task = taskQueue.poll(1500, TimeUnit.MILLISECONDS);
						
						execute(task);
					}
					catch (InterruptedException e)
					{
						runnable.set(false);
						e.printStackTrace();
						
						break;
					}
				}
			}
		}, DigestTask.class);
		
		return true;
	}
	
	private void execute(final DigestTask task)
	{
		service.submit(new Runnable()
		{
			
			@Override
			public void run()
			{
				task.setStartTime(System.currentTimeMillis());
				
				MessageDigest digest;
				try
				{
					digest = MessageDigest.getInstance(task.getType());
					
					File file = task.getFile();
					if(file != null && file.canRead())
					{
						DigestInputStream inStream = new DigestInputStream(
								new FileInputStream(file), digest);
						
						byte[] buf = new byte[10240];
						int len = -1;
						double total = 0;
						long length = file.length();
						
						while((len = inStream.read(buf)) != -1)
						{
							total += len;
							
							task.setLevel((int) (total / length * 100));
						}
						
						inStream.close();
						
						byte[] result = digest.digest();
						StringBuffer buffer = new StringBuffer();
						for(byte b : result)
						{
							buffer.append(Integer.toHexString(b & 0xff));
						}
						
						task.setResult(buffer.toString());
					}
				}
				catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				task.setEndTime(System.currentTimeMillis());
			}
		});
	}
	
	public boolean addTask(DigestTask task)
	{
		return taskQueue.offer(task);
	}

	public boolean isInited()
	{
		return inited;
	}

	public void setInited(boolean inited)
	{
		this.inited = inited;
	}
}
