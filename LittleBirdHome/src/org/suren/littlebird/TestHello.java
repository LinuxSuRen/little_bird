package org.suren.littlebird;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

public class TestHello implements TestMXBean
{

	@Override
	public void hello()
	{
		System.out.println("hello jmx");
		System.gc();
		
		ManagementFactory.getMemoryMXBean().gc();
		System.out.println(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
		
		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
		System.out.println(oper.getAvailableProcessors());
		
		List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
		for(GarbageCollectorMXBean gc : gcs)
		{
			System.out.println(gc.getName() + "---" + gc.getCollectionCount());
		}
		
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		for(String arg : runtime.getInputArguments())
		{
			System.out.println(arg);
		}
		
		ThreadMXBean thread = ManagementFactory.getThreadMXBean();
		System.out.println("thread count : " + thread.getThreadCount());
	}

	@Override
	public String getName()
	{
		return "get name";
	}

}
