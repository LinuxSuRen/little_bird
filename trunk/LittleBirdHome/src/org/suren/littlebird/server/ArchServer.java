package org.suren.littlebird.server;

public interface ArchServer extends Runnable
{
	public boolean init(int port);
	public boolean isInited();
	public boolean stop();
}
