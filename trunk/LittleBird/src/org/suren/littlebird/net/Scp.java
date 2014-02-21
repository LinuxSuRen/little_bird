package org.suren.littlebird.net;

import java.io.Serializable;

public interface Scp
{
	public boolean push(String ... path) throws Exception;
	
	public boolean push(String host, String path) throws Exception;
	
	public boolean push(User user, boolean async) throws Exception;
	
	public boolean push(User user) throws Exception;
	
	public class User implements Serializable
	{
		private static final long	serialVersionUID	= -6091052792387599324L;
		
		private String user;
		private String password;
		private String host;
		private String localPath;
		private String remotePath;
		
		public String getUser()
		{
			return user;
		}
		
		public void setUser(String user)
		{
			this.user = user;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		public String getHost()
		{
			return host;
		}

		public void setHost(String host)
		{
			this.host = host;
		}

		public String getLocalPath()
		{
			return localPath;
		}

		public void setLocalPath(String localPath)
		{
			this.localPath = localPath;
		}

		public String getRemotePath()
		{
			return remotePath;
		}

		public void setRemotePath(String remotePath)
		{
			this.remotePath = remotePath;
		}

		@Override
		public int hashCode()
		{
			int hashCode = 0;
			
			if(user != null)
			{
				hashCode += user.hashCode();
			}
			
			if(password != null)
			{
				hashCode += user.hashCode();
			}
			
			if(host != null)
			{
				hashCode += user.hashCode();
			}
			
			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj == this)
			{
				return true;
			}
			
			if(obj instanceof User)
			{
				User userObj = (User) obj;
				
				if(userObj.hashCode() == hashCode())
				{
					return true;
				}
			}
			
			return false;
		}
	}
}
