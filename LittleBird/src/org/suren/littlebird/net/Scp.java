package org.suren.littlebird.net;

public interface Scp
{
	public boolean push(String ... path) throws Exception;
	
	public boolean push(String address, String path) throws Exception;
	
	public boolean push(String address, String remote, String local, boolean async) throws Exception;
	
	public boolean push(String address, String remote, String local) throws Exception;
}
