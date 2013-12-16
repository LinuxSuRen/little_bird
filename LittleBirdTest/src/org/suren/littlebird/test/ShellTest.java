package org.suren.littlebird.test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.suren.littlebird.shell.Shell;

public class ShellTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Shell shell = (Shell)Naming.lookup("rmi://10.0.32.3:8989/Shell");
			
			for(int i = 0; i < 10; i++)
			{
				String env = shell.getEnv("PATH");
				
				System.out.println(env);;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

}
