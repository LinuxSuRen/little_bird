package org.suren.littlebird.server;

import java.rmi.Remote;

public interface Server extends Remote
{
	public  String getName() throws Exception;
}
