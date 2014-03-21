package org.suren.littlebird.listener;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class SuRenServiceListener implements ServiceListener, SuRenListener
{
	private Logger logger = Logger.getLogger(SuRenServiceListener.class);

	public void serviceChanged(ServiceEvent event)
	{
		int code = event.getType();

		logger.trace("Service Event Happened, type : " + getTypeName(code) +
				" - " + event.getServiceReference().getBundle().getSymbolicName());
	}

	public String getTypeName(int type)
	{
		String typeName = null;

		switch(type)
		{
			case ServiceEvent.REGISTERED:
				typeName = "registred";
				break;
			case ServiceEvent.MODIFIED:
				typeName = "modified";
				break;
			case ServiceEvent.UNREGISTERING:
				typeName = "unregistering";
				break;
			case ServiceEvent.MODIFIED_ENDMATCH:
				typeName = "modified_endmatch";
				break;
			default:
				typeName = "unknow type : " + type;
				break;
		}

		return typeName;
	}

}
