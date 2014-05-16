package org.suren.littlebird.listener;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class FrameListener implements FrameworkListener
{

	public void frameworkEvent(FrameworkEvent event)
	{
		System.out.println(event.getSource().getClass() + "=====" + event.getBundle() + "====" + event.getType());
	}

}
