package org.suren.littlebird.setting;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OsgiMgrSetting
{
	private String host;
	private int port;
	@XmlElement
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	@XmlElement
	public int getPort()
	{
		return port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}
}
