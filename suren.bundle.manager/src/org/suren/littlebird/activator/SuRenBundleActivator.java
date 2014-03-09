package org.suren.littlebird.activator;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.DefaultBundleServer;
import org.suren.littlebird.server.DefaultLoggerServer;
import org.suren.littlebird.server.LoggerServer;
import org.suren.littlebird.server.Server;

public class SuRenBundleActivator implements BundleActivator
{
	private BundleContext context;
	private List<ServiceRegistration> registrationList;

	private final String addrValue = "http://localhost:9789/";
	
	private Logger logger = Logger.getLogger(SuRenBundleActivator.class);

	public void start(BundleContext context) throws Exception
	{
		this.context = context;

		BundleServer bundleServer = new DefaultBundleServer(context);
		LoggerServer loggerServer = new DefaultLoggerServer();

		registrationList = new ArrayList<ServiceRegistration>();

		register(bundleServer, BundleServer.class);
		register(loggerServer, LoggerServer.class);
		
		logger.info("SuRen Bundle Started.");
	}

	private void register(Server server, Class<?> clazz)
	{
		if(server == null)
		{
			return;
		}

		Dictionary<String, String> properties = new Hashtable<String, String>();

//		properties.put("osgi.remote.interfaces", "*");
//		properties.put("osgi.remote.configuration.type", "pojo");
//		properties.put(addrKey, addrValue + server.getName());
		
		properties.put("service.exported.interfaces", "*");
		properties.put("service.exported.intents", "SOAP");
		properties.put("service.exported.configs", "org.apache.cxf.ws");
		properties.put("org.apache.cxf.ws.address", addrValue + server.getName());
		
		logger.info(addrValue + server.getName());

		ServiceRegistration registration = context.registerService(clazz.getName(), server, properties);
		registrationList.add(registration);
	}

	public void stop(BundleContext context) throws Exception
	{
		for(ServiceRegistration registration : registrationList)
		{
			if(registration != null)
			{
				registration.unregister();
			}
		}
		
		logger.info("SuRen Bundle Stoped.");
	}
}
