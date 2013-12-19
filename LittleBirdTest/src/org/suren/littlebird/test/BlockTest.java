package org.suren.littlebird.test;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public class BlockTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(10);
		
		queue.add("hoa");
		queue.add("test");
		queue.add(null);
		
		System.out.println(queue.size());
		System.out.println(queue.remainingCapacity());
		
		Iterator<Object> it = queue.iterator();
		while(it.hasNext())
		{
			System.out.println(it.next());
		}
		
		System.out.println(queue.size());
		System.out.println(queue.remainingCapacity());
	}

}
