package org.suren.littlebird.setting;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SshSetting
{
	private String alias;
	private String user;
	private String host;
	private int port;
	private String path;
	private Set<String> historyPath;
	@XmlElement
	public String getAlias()
	{
		return alias;
	}
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	@XmlElement
	public String getUser()
	{
		return user;
	}
	public void setUser(String user)
	{
		this.user = user;
	}
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
	public void setHistoryPath(Set<String> historyPath)
	{
		this.historyPath = historyPath;
	}
}
