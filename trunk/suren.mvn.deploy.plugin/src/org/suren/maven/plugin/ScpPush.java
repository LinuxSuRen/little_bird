package org.suren.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.suren.littlebird.net.Scp;
import org.suren.littlebird.net.ScpRmi;

@Mojo(name = "scp_push")
public class ScpPush extends SuRenMojo
{
	@Parameter(property = "address", defaultValue = "127.0.0.1")
	private String address;
	@Parameter(property = "port", defaultValue = "6600")
	private String port;

	@Parameter
	private String host;
	@Parameter
	private String user;
	@Parameter
	private String password;
	@Parameter
	private String remotePath;
	@Parameter(defaultValue = TRUE)
	private String async;

	private Log log;
	private File jarFile;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		log = getLog();
		jarFile = getJar();

		if(!jarFile.isFile())
		{
			log.warn("jarFile not exists.");

			return;
		}

		String path = jarFile.getAbsolutePath();

		log.info("prepare to push jar : " + path);

		String rmiAddr = "rmi://" + address + ":" + port +"/";

		try
		{
			ScpRmi scp = (ScpRmi) Naming.lookup(rmiAddr + SERVICE);

			if(isDirectPush(scp))
			{
				log.info("direct push done.");
			}
			else
			{
				boolean result = scp.push(path);

				log.info("scp push result : " + result);
			}
		}
		catch (MalformedURLException e)
		{
			log.warn("unknow address : "+ rmiAddr);
		}
		catch(java.rmi.ConnectException e)
		{
			log.warn("can not connect server.");
		}
		catch (RemoteException e)
		{
			log.error(e);
		}
		catch (NotBoundException e)
		{
			log.warn("can not found service : " + SERVICE);
		}
		catch (Exception e)
		{
			log.error(e);
		}
	}

	private boolean isDirectPush(ScpRmi scp)
	{
		Scp.User user = new Scp.User();

		if(getHost() == null || getUser() == null ||
				getPassword() == null || getRemotePath() == null)
		{
			return false;
		}

		user.setHost(getHost());
		user.setUser(getUser());
		user.setPassword(getPassword());
		user.setLocalPath(jarFile.getAbsolutePath());
		user.setRemotePath(getRemotePath());

		try
		{
			if(FALSE.equals(async))
			{
				scp.push(user, false);
			}
			else
			{
				scp.push(user);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

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

	public String getRemotePath()
	{
		return remotePath;
	}

	public void setRemotePath(String remotePath)
	{
		this.remotePath = remotePath;
	}

	public String getAsync()
	{
		return async;
	}

	public void setAsync(String async)
	{
		this.async = async;
	}
}
