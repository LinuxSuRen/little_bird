package org.suren.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "auto_exec")
public class AutoExec extends AbstractMojo
{
	@Parameter(property = "cmdList", required = true)
	private String cmdList;
	@Parameter(property = "project")
	private MavenProject project;
	
	private Log log;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		log = getLog();
		
		log.info("cmdList : " + cmdList);
		
		String[] cmdArray = cmdList.split(",");
		for(String cmd : cmdArray)
		{
			exeCmd(cmd);
		}
	}

	private void exeCmd(String cmd)
	{
		Runtime runtime = Runtime.getRuntime();
		
		try
		{
			Process process = runtime.exec(cmd);
			
			InputStream input = process.getInputStream();
			
			byte[] buf = new byte[1024];
			int len = -1;
			while((len = input.read(buf)) != -1)
			{
				log.info(new String(buf, 0, len));
			}
		}
		catch (IOException e)
		{
			log.error(e);
		}
	}

	public String getCmdList()
	{
		return cmdList;
	}

	public void setCmdList(String cmdList)
	{
		this.cmdList = cmdList;
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
