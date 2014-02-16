package org.suren.littlebird.net.ssh;

import com.jcraft.jsch.SftpProgressMonitor;

public abstract class SimpleSftpProgressMonitor<T> implements SftpProgressMonitor
{
	private T target;

	public T getTarget()
	{
		return target;
	}

	public void setTarget(T target)
	{
		this.target = target;
	}
}
