package org.suren.littlebird.server;

import org.osgi.framework.Bundle;

public class BundleConvert
{
	public static void toSuRen(Bundle bundle, SuRenBundle surenBundle)
	{
		if(bundle == null || surenBundle == null)
		{
			return;
		}

		surenBundle.setId(bundle.getBundleId());
		surenBundle.setName(bundle.getSymbolicName());
		surenBundle.setLocation(bundle.getLocation());
		surenBundle.setState(bundle.getState());
		surenBundle.setLastModified(bundle.getLastModified());
	}
}
