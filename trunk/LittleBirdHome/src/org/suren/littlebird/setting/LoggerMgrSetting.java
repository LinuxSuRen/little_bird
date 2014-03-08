package org.suren.littlebird.setting;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoggerMgrSetting
{
	private String host;
	private int port;
	private int bridgePort;
	private boolean lineWrap;
	private int backColor;
	private int foreColor;
	private int consoleBuffer;
	private String logLayout;
	private Set<String> historyKeyword;
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
	public int getBridgePort()
	{
		return bridgePort;
	}
	public void setBridgePort(int bridgePort)
	{
		this.bridgePort = bridgePort;
	}
	@XmlElement
	public boolean isLineWrap()
	{
		return lineWrap;
	}
	public void setLineWrap(boolean lineWrap)
	{
		this.lineWrap = lineWrap;
	}
	@XmlElement
	public int getBackColor()
	{
		return backColor;
	}
	public void setBackColor(int backColor)
	{
		this.backColor = backColor;
	}
	@XmlElement
	public int getForeColor()
	{
		return foreColor;
	}
	public void setForeColor(int foreColor)
	{
		this.foreColor = foreColor;
	}
	@XmlElement
	public int getConsoleBuffer()
	{
		if(consoleBuffer < 20)
		{
			setConsoleBuffer(20);
		}
		
		return consoleBuffer;
	}
	public void setConsoleBuffer(int consoleBuffer)
	{
		this.consoleBuffer = consoleBuffer;
	}
	@XmlElement
	public String getLogLayout()
	{
		return logLayout;
	}
	public void setLogLayout(String logLayout)
	{
		this.logLayout = logLayout;
	}
	@XmlElementWrapper
	@XmlElement(name = "keyword")
	public Set<String> getHistoryKeyword()
	{
		return historyKeyword;
	}
	public void addHistoryKeyword(String keyword)
	{
		if(getHistoryKeyword() == null)
		{
			setHistoryKeyword(new HashSet<String>());
		}
		
		getHistoryKeyword().add(keyword);
	}
	public void setHistoryKeyword(Set<String> historyKeyword)
	{
		this.historyKeyword = historyKeyword;
	}
}
