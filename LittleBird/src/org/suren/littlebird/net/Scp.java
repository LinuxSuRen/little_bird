package org.suren.littlebird.net;

public interface Scp
{
	public boolean push(String ... path) throws Exception;
	
	public boolean push(String address, String path) throws Exception;
	
	public boolean push(User user, boolean async) throws Exception;
	
	public boolean push(User user) throws Exception;
	
	public class User
	{
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
	}
}
