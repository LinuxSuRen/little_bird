package org.suren.littlebird.disk;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Disk extends Remote
{
	public File[] root() throws RemoteException;
}
