package org.suren.littlebird.listener;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class TestListener implements BundleListener
{

	public void bundleChanged(BundleEvent event)
	{
		System.out.println(event.getSource().getClass() + "===" + event.getBundle().getLocation() + "===" + event.getType());
	}

}
