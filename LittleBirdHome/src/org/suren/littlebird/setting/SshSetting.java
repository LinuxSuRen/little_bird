package org.suren.littlebird.setting;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class SshSetting
{
	private boolean prompt;
	private String lastHost;
	private Set<Ssh> sshList;

	@XmlElement
	public boolean isPrompt()
	{
		return prompt;
	}

	public void setPrompt(boolean prompt)
	{
		this.prompt = prompt;
	}

	@XmlElement
	public String getLastHost()
	{
		return lastHost;
	}

	public void setLastHost(String lastHost)
	{
		this.lastHost = lastHost;
	}

//	@XmlElementWrapper
//	@XmlElement(name = "ssh")
	@XmlJavaTypeAdapter(ListAdapter.class)
	public Set<Ssh> getSshList()
	{
		return sshList;
	}

	public void setSshList(Set<Ssh> sshList)
	{
		this.sshList = sshList;
	}
	
	public void addSsh(Ssh ssh)
	{
		if(ssh == null)
		{
			return;
		}
		
		if(sshList == null)
		{
			sshList = new HashSet<Ssh>();
		}
		
		sshList.add(ssh);
	}

	@XmlRootElement
	public static class Ssh
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
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Ssh other = (Ssh) obj;
			if (host == null)
			{
				if (other.host != null)
					return false;
			}
			else if (!host.equals(other.host))
				return false;
			return true;
		}
	}
}
