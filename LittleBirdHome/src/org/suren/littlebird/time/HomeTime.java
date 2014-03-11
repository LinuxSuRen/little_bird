package org.suren.littlebird.time;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import org.suren.littlebird.annotation.Publish;

@Publish(name = "Time")
public class HomeTime extends UnicastRemoteObject implements Time {

	public HomeTime() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3354955374801926695L;

	@Override
	public Date getDate() throws RemoteException
	{
		return new Date();
	}

}
