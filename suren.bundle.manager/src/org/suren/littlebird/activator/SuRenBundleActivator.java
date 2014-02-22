package org.suren.littlebird.activator;

import java.rmi.registry.Registry;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.DefaultBundleServer;

public class SuRenBundleActivator implements BundleActivator
{
	private Registry registry;
	ServiceRegistration registration;

	public void start(BundleContext context) throws Exception
	{
		BundleServer bundleServer = new DefaultBundleServer(context);
		Dictionary properties = new Properties();

		properties.put("osgi.remote.interfaces", "*");
		properties.put("osgi.remote.configuration.type", "pojo");
		properties.put("osgi.remote.configuration.pojo.address", "http://localhost:6789/greeter");

		registration = context.registerService(BundleServer.class.getName(), bundleServer, properties );
	}

	public void stop(BundleContext context) throws Exception
	{
		if(registry != null)
		{
			registration.unregister();
		}
	}
}