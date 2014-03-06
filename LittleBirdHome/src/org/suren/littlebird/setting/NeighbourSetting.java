package org.suren.littlebird.setting;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.internal.txw2.annotation.XmlElement;

@XmlRootElement
public class NeighbourSetting
{
	private String fromHost;
	private String endHost;
	private int timeout;
	@XmlElement
	public String getFromHost()
	{
		return fromHost;
	}
	public void setFromHost(String fromHost)
	{
		this.fromHost = fromHost;
	}
	@XmlElement
	public String getEndHost()
	{
		return endHost;
	}
	public void setEndHost(String endHost)
	{
		this.endHost = endHost;
	}
	@XmlElement
	public int getTimeout()
	{
		return timeout;
	}
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
}
