package org.suren.littlebird.net.webservice;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.suren.littlebird.exception.SuRenSettingException;

public class ClientProxy<T>
{
	private String url;
	
	public ClientProxy(String url, int port, String name)
	{
		if(url == null || "".equals(url) || port < 0 || port > 65536)
		{
			throw new SuRenSettingException("webservice url or port is invalid.");
		}
		
		String host = url + ":" + port + "/" + name;
		if(!host.startsWith("http://") && !host.startsWith("https://"))
		{
			host = "http://" + host;
		}
		
		this.url = host;
	}
	
	public ClientProxyFactoryBean getClientProxy(Class<T> clazz)
	{
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		if(url == null || "".equals(url))
		{
			throw new SuRenSettingException("webservice url is invalid.");
		}
		
		factory.setAddress(url);
		factory.setServiceClass(clazz);
		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		
		return factory;
	}
}
