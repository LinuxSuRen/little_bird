package org.suren.littlebird.net;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.suren.littlebird.annotation.Publish;
import org.suren.littlebird.net.ssh.SimpleUserInfo;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Publish(name = "Scp")
public class HomeScp extends UnicastRemoteObject implements ScpRmi
{
	private static final long	serialVersionUID	= 5194088841214703659L;
	public static Set<File> tmpPath = new HashSet<File>();
	
	private JSch jsch = new JSch();
	private Map<Integer, Session> sessionMap = new HashMap<Integer, Session>();
	
	public HomeScp() throws RemoteException
	{
		super();
	}

	@Override
	public boolean push(String... path)
	{
		if(path == null)
		{
			return false;
		}
		
		for(String item : path)
		{
			File file = new File(item);

			if(file.isFile() && file.exists())
			{
				tmpPath.add(file);
			}
		}
		
		return true;
	}

	@Override
	public boolean push(String host, String path) throws Exception
	{
		return false;
	}

	@Override
	public boolean push(User user, boolean async) throws Exception
	{
		if(user == null)
		{
			return false;
		}
		
		Session session = getAvailableSession(user);
		if(session != null)
		{
			ChannelSftp ftp = (ChannelSftp) session.openChannel("sftp");
			
			asyncRun.setUser(user);
			asyncRun.setChannel(ftp);
			
			if(async)
			{
				new Thread(asyncRun, "AsyncRun").start();
			}
			else
			{
				asyncRun.run();
			}
		}
		
		return true;
	}

	@Override
	public boolean push(User user) throws Exception
	{
		return push(user, true);
	}

	private Session getAvailableSession(User user)
	{
		int hashCode = user.hashCode();
		Session session = sessionMap.get(hashCode);
		if(session == null || !session.isConnected())
		{
			session = openSession(user);
			sessionMap.put(hashCode, session);
		}
		
		return session;
	}

	private Session openSession(User user)
	{
		try
		{
			Session session = jsch.getSession(user.getUser(), user.getHost());
			
			SimpleUserInfo userInfo = new SimpleUserInfo();
			userInfo.setPassword(user.getPassword());
			
			session.setUserInfo(userInfo);
			session.connect();
			
			return session;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private AsyncRun asyncRun = new AsyncRun()
	{
		
		@Override
		public void run()
		{
			String local = getUser().getLocalPath();
			if(!new File(local).isFile())
			{
				return;
			}
			
			ChannelSftp channel = getChannel();
			
			try
			{
				channel.connect();
				channel.put(local, getUser().getRemotePath());
				channel.disconnect();
			}
			catch (SftpException e)
			{
				e.printStackTrace();
			}
			catch (JSchException e)
			{
				e.printStackTrace();
			}
		}
	};
	
	abstract class AsyncRun implements Runnable
	{
		private User user;
		private ChannelSftp channel;

		public User getUser()
		{
			return user;
		}

		public void setUser(User user)
		{
			this.user = user;
		}

		public ChannelSftp getChannel()
		{
			return channel;
		}

		public void setChannel(ChannelSftp channel)
		{
			this.channel = channel;
		}
	}
}
