package org.suren.littlebird.disk;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Disk")
public class HomeDisk extends UnicastRemoteObject implements Disk {

	public HomeDisk() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 611943208083731391L;

	@Override
	public File[] root() throws RemoteException {
		return File.listRoots();
	}

}
