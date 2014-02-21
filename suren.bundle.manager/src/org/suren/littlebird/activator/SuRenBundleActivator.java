package org.suren.littlebird.activator;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.DefaultBundleServer;

public class SuRenBundleActivator implements BundleActivator
{
	private BundleContext context;
	private Registry registry;
	private final int port = 8789;
	ServiceRegistration registration;

	public static void main(String[] args) throws Exception
	{
		SuRenBundleActivator obj = new SuRenBundleActivator();
		obj.start(null);
		obj.stop(null);
	}

	public void start(BundleContext context) throws Exception
	{
		this.context = context;

		BundleServer bundleServer = new DefaultBundleServer(context);
		Dictionary properties = new Properties();

		properties.put("osgi.remote.interfaces", "*");
		properties.put("osgi.remote.configuration.type", "pojo");
		properties.put("osgi.remote.configuration.pojo.address", "http://localhost:6789/greeter");

		registration = context.registerService(BundleServer.class.getName(), bundleServer, properties );

//		tryToRegister();
	}

	public void stop(BundleContext context) throws Exception
	{
		if(registry != null)
		{
			UnicastRemoteObject.unexportObject(registry, true);
		}

//		registration.unregister();
	}
}
