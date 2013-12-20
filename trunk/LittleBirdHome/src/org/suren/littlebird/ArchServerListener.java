package org.suren.littlebird;

public interface ArchServerListener
{
	public void onLine(ClientInfo clientInfo);
	
	public void offLine(ClientInfo clientInfo);
}
