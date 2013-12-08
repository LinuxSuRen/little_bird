package org.suren.littlebird.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Shell extends Remote
{
	public boolean run(String cmd) throws RemoteException;
	
	public String getEnv(String name) throws RemoteException;
}
