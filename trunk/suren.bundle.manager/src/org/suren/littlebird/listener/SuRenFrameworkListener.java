package org.suren.littlebird.listener;

import org.apache.log4j.Logger;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class SuRenFrameworkListener implements FrameworkListener, SuRenListener
{
	private Logger logger = Logger.getLogger(SuRenFrameworkListener.class);

	public void frameworkEvent(FrameworkEvent event)
	{
		int code = event.getType();

		logger.trace("Framework Event Happened, type : " + getTypeName(code));
	}

	public String getTypeName(int type)
	{
		String typeName = null;

		switch(type)
		{
			case FrameworkEvent.STARTED:
				typeName = "started";
				break;
			case FrameworkEvent.ERROR:
				typeName = "error";
				break;
			case FrameworkEvent.PACKAGES_REFRESHED:
				typeName = "packages_refreshed";
				break;
			case FrameworkEvent.STARTLEVEL_CHANGED:
				typeName = "startlevel_changed";
				break;
			case FrameworkEvent.WARNING:
				typeName = "warning";
				break;
			case FrameworkEvent.INFO:
				typeName = "info";
				break;
			case FrameworkEvent.STOPPED:
				typeName = "stopped";
				break;
			case FrameworkEvent.STOPPED_UPDATE:
				typeName = "stopped_update";
				break;
			case FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED:
				typeName = "stopped_boootclasspath_modified";
				break;
			case FrameworkEvent.WAIT_TIMEDOUT:
				typeName = "wait_timeout";
				break;
			default:
				typeName = "unknow type : " + type;
				break;
		}

		return typeName;
	}

}
