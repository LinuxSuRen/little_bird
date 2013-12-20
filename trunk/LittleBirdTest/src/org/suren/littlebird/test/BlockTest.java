package org.suren.littlebird.test;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockTest
{

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException
	{
		final ArrayBlockingQueue<Test> queue = new ArrayBlockingQueue<Test>(2);
		
		Test test = new Test();
		queue.offer(test);
		queue.offer(new Test());
		
		new Thread()
		{
			public void run()
			{
				try
				{
					queue.offer(new Test(), 99999999, TimeUnit.DAYS);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
		
		Iterator<Test> it = queue.iterator();
		while(it.hasNext())
		{
			System.out.println(it.next() + "---");
		}
	}
	
	static class Test
	{
		public int abc = 12;
	}

}
