package org.suren.littlebird.net;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Network extends Remote
{
	public String[] nicList() throws RemoteException;
}
