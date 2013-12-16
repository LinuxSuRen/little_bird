package org.suren.littlebird.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		java.net.Socket socket = new Socket();
		
//		socket.setSoTimeout(timeout)
		SocketAddress address = new InetSocketAddress("10.0.3", 22);
		
		long begin = System.currentTimeMillis();
		try {
//			socket.setSoTimeout(3000);
			socket.connect(address, 3000);
			
			System.out.println("connected : " + (System.currentTimeMillis() - begin));
			
			socket.close();
			socket.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException : " + (System.currentTimeMillis() - begin));
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException : " + (System.currentTimeMillis() - begin));
		}
	}

}
