package org.suren.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.suren.littlebird.net.ScpRmi;

@Mojo(name = "scp_push")
public class ScpPush extends AbstractMojo
{
	@Parameter(property = "address", defaultValue="127.0.0.1")
	private String address;
	@Parameter(property = "port", required = true)
	private String port;

	@Parameter(property = "project")
	private MavenProject project;

	private Log log;

	private static final String SERVICE = "HomeScp";

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		log = getLog();

		Build build = project.getBuild();
		String finalName = build.getFinalName();
		String dir = build.getDirectory();

		File file = new File(dir, finalName + ".jar");

		log.info("prepare to push jar : " + file.getAbsolutePath());

		String rmiAddr = "rmi://" + address + ":" + port +"/";

		try
		{
			ScpRmi scp = (ScpRmi) Naming.lookup(rmiAddr + SERVICE);

			boolean result = scp.push(file.getAbsolutePath());

			log.info("scp push result : " + result);
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

	public MavenProject getProject()
	{
		return project;
	}

	public void setProject(MavenProject project)
	{
		this.project = project;
	}
}
