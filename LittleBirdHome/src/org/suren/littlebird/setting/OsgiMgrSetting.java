package org.suren.littlebird.setting;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.suren.littlebird.util.CommonConstant;

@XmlRootElement
public class OsgiMgrSetting
{
	private String host;
	private int port = CommonConstant.OSGI_SERVER;
	private int sshPort = CommonConstant.SSH_PORT;
	private String sshUser;
	private String sshPwd;
	private int outputBuffer = CommonConstant.OUTPUT_BUF;
	private String path;
	private Set<String> historyPath;
	private Set<String> historyUrl;
	private Set<String> historyRemote;
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
	public int getSshPort()
	{
		return sshPort;
	}
	public void setSshPort(int sshPort)
	{
		this.sshPort = sshPort;
	}
	@XmlElement
	public String getSshUser()
	{
		return sshUser;
	}
	public void setSshUser(String sshUser)
	{
		this.sshUser = sshUser;
	}
	@XmlElement
	public String getSshPwd()
	{
		return sshPwd;
	}
	public void setSshPwd(String sshPwd)
	{
		this.sshPwd = sshPwd;
	}
	public int getOutputBuffer()
	{
		return outputBuffer;
	}
	public void setOutputBuffer(int outputBuffer)
	{
		this.outputBuffer = outputBuffer;
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
	@XmlElementWrapper
	@XmlElement(name = "remote")
	public Set<String> getHistoryRemote()
	{
		return historyRemote;
	}
	public void addHistoryRemote(String path)
	{
		if(getHistoryRemote() == null)
		{
			setHistoryRemote(new HashSet<String>());
		}
		
		getHistoryRemote().add(path);
	}
	public void setHistoryRemote(Set<String> historyRemote)
	{
		this.historyRemote = historyRemote;
	}
}
