package org.suren.littlebird.disk;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Disk")
public class HomeDisk extends UnicastRemoteObject implements Disk {

	private static final long serialVersionUID = 611943208083731391L;

	public HomeDisk() throws RemoteException {
		super();
	}

	@Override
	public File[] root() throws RemoteException {
		return File.listRoots();
	}

}
