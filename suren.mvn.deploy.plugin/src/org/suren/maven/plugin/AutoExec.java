package org.suren.maven.plugin;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "auto_exec")
public class AutoExec extends SuRenMojo
{
	@Parameter(property = "cmdList", required = true)
	private String cmdList;
	@Parameter(defaultValue = FALSE)
	private String async;
	@Parameter(defaultValue = TRUE)
	private String print;
	@Parameter(defaultValue = "GBK")
	private String sourceCharset;
	@Parameter(defaultValue = "UTF-8")
	private String targetCharset;

	private Log log;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		log = getLog();

		log.info("cmdList : " + cmdList);

		if(FALSE.equals(getAsync()))
		{
			run.run();
		}
		else if(TRUE.equals(getAsync()))
		{
			new Thread(run, "async run cmd").start();
		}
		else
		{
			log.warn("invalid async flag.");
		}
	}

	private Runnable run = new Runnable()
	{

		public void run()
		{
			String[] cmdArray = cmdList.split(",");
			for(String cmd : cmdArray)
			{
				exeCmd(cmd);
			}
		}
	};

	private void exeCmd(String cmd)
	{
		Runtime runtime = Runtime.getRuntime();

		log.info("prepare to exec : " + cmd);

		try
		{
			Process process = runtime.exec(cmd);

			InputStream input = process.getInputStream();

			byte[] buf = new byte[1024];
			int len = -1;
			while((len = input.read(buf)) != -1)
			{
				String msg = new String(buf, 0, len, sourceCharset);
				log.info(new String(msg.getBytes(targetCharset)));
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

	public String getAsync()
	{
		return async;
	}

	public void setAsync(String async)
	{
		this.async = async;
	}

	public String getSourceCharset()
	{
		return sourceCharset;
	}

	public void setSourceCharset(String sourceCharset)
	{
		this.sourceCharset = sourceCharset;
	}

	public String getTargetCharset()
	{
		return targetCharset;
	}

	public void setTargetCharset(String targetCharset)
	{
		this.targetCharset = targetCharset;
	}

	public String getPrint()
	{
		return print;
	}

	public void setPrint(String print)
	{
		this.print = print;
	}
}
