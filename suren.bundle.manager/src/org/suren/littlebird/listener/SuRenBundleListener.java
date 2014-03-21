package org.suren.littlebird.listener;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class SuRenBundleListener implements BundleListener, SuRenListener
{
	private Logger logger = Logger.getLogger(SuRenBundleListener.class);

	public void bundleChanged(BundleEvent event)
	{
		int code = event.getType();

		logger.trace("Bundle Event Happened, type : " + getTypeName(code) +
				" - " + 		event.getBundle().getSymbolicName());
	}

	public String getTypeName(int type)
	{
		String typeName = null;

		switch(type)
		{
			case BundleEvent.INSTALLED:
				typeName = "installed";
				break;
			case BundleEvent.STARTED:
				typeName = "started";
				break;
			case BundleEvent.STOPPED:
				typeName = "stopped";
				break;
			case BundleEvent.UPDATED:
				typeName = "updated";
				break;
			case BundleEvent.UNINSTALLED:
				typeName = "uninstalled";
				break;
			case BundleEvent.RESOLVED:
				typeName = "resolved";
				break;
			case BundleEvent.UNRESOLVED:
				typeName = "unresolved";
				break;
			case BundleEvent.STARTING:
				typeName = "starting";
				break;
			case BundleEvent.STOPPING:
				typeName = "stopping";
				break;
			case BundleEvent.LAZY_ACTIVATION:
				typeName = "lazy_activation";
				break;
			default:
				typeName = "unknow type : " + type;
				break;
		}

		return typeName;
	}

}
