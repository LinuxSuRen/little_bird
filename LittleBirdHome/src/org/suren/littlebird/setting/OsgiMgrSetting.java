package org.suren.littlebird.setting;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OsgiMgrSetting
{
	private String host;
	private int port;
	private String path;
	private Set<String> historyPath;
	private Set<String> historyUrl;
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
	@XmlElement
	public String getPath()
	{
		return path;
	}
	public void setPath(String path)
	{
		this.path = path;
	}
	@XmlElementWrapper
	@XmlElement(name = "path")
	public Set<String> getHistoryPath()
	{
		return historyPath;
	}
	public void addHistoryPath(String path)
	{
		if(getHistoryPath() == null)
		{
			setHistoryPath(new HashSet<String>());
		}
		
		getHistoryPath().add(path);
	}
	public void setHistoryPath(Set<String> historyPath)
	{
		this.historyPath = historyPath;
	}
	@XmlElementWrapper
	@XmlElement(name = "url")
	public Set<String> getHistoryUrl()
	{
		return historyUrl;
	}
	public void addHistoryUrl(String url)
	{
		if(getHistoryUrl() == null)
		{
			setHistoryUrl(new HashSet<String>());
		}
		
		getHistoryUrl().add(url);
	}
	public void setHistoryUrl(Set<String> historyUrl)
	{
		this.historyUrl = historyUrl;
	}
}
