package org.suren.littlebird.server;

import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

public class BundleConvert
{
	public static void toSuRen(Bundle bundle, SuRenBundle surenBundle)
	{
		toSuRen(bundle, surenBundle, true);
	}

	public static void toSuRen(Bundle bundle, SuRenBundle surenBundle, boolean lazy)
	{
		if(bundle == null || surenBundle == null)
		{
			return;
		}

		surenBundle.setId(bundle.getBundleId());
		surenBundle.setName(bundle.getSymbolicName());
		surenBundle.setLocation(bundle.getLocation());
		surenBundle.setVersion(bundle.getVersion().toString());
		surenBundle.setState(bundle.getState());
		surenBundle.setLastModified(bundle.getLastModified());

		if(!lazy)
		{
			Dictionary<?, ?> headers = bundle.getHeaders();
			Enumeration<?> keys = headers.keys();
			BundleHeader[] bundleHeaders = new BundleHeader[headers.size()];
			int index = 0;
			while(keys.hasMoreElements())
			{
				Object key = keys.nextElement();
				Object value = headers.get(key);

				BundleHeader bundleRef = new BundleHeader();

				bundleRef.setKey(key);
				bundleRef.setValue(value);

				bundleHeaders[index++] = bundleRef;
			}
			surenBundle.setHeaders(bundleHeaders);

			surenBundle.setServiceRef(new ServiceInfo[0]);
		}
	}
}
