package org.suren.littlebird.listener;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class TestServiceListener implements ServiceListener
{

	public void serviceChanged(ServiceEvent event)
	{
		System.out.println(event.getSource().getClass() + "===" + event.getServiceReference() + "===" + event.getType());
	}

}
