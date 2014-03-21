package org.suren.littlebird.activator;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.suren.littlebird.listener.SuRenBundleListener;
import org.suren.littlebird.listener.SuRenFrameworkListener;
import org.suren.littlebird.listener.SuRenServiceListener;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.DefaultBundleServer;
import org.suren.littlebird.server.DefaultLoggerServer;
import org.suren.littlebird.server.LoggerServer;
import org.suren.littlebird.server.Server;

public class SuRenBundleActivator implements BundleActivator
{
	private BundleContext context;
	private List<ServiceRegistration> registrationList;

	private BundleListener surenBundleListener;
	private FrameworkListener surenFrameworkListener;
	private ServiceListener surenServiceListener;

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

		listenerRegister();

		logger.info("SuRen Bundle Started.");
	}

	private void listenerRegister()
	{
		surenBundleListener = new SuRenBundleListener();
		surenFrameworkListener = new SuRenFrameworkListener();
		surenServiceListener = new SuRenServiceListener();

		context.addBundleListener(surenBundleListener);
		context.addFrameworkListener(surenFrameworkListener);
		context.addServiceListener(surenServiceListener);

		logger.info("SuRenListener Regist Over.");
	}

	private void register(Server server, Class<?> clazz)
	{
		String name = null;
		if(server == null)
		{
			return;
		}

		try
		{
			name = server.getName();
		}
		catch(Exception e)
		{
			logger.error("get name error.", e);
		}

		if(name == null)
		{
			return;
		}

		Dictionary<String, String> properties = new Hashtable<String, String>();

		properties.put("osgi.remote.interfaces", "*");
		properties.put("osgi.remote.configuration.type", "pojo");
		properties.put("osgi.remote.configuration.pojo.address", addrValue + name);

		properties.put("service.exported.interfaces", "*");
		properties.put("service.exported.intents", "SOAP");
		properties.put("service.exported.configs", "org.apache.cxf.ws");
		properties.put("org.apache.cxf.ws.address", addrValue + name);

		logger.info(addrValue + name);

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

		context.removeBundleListener(surenBundleListener);
		context.removeFrameworkListener(surenFrameworkListener);
		context.removeServiceListener(surenServiceListener);

		logger.info("SuRen Bundle Stoped.");
	}
}
