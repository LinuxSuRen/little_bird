package org.suren.littlebird.test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MonitorTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket socket = new Socket("10.0.32.3", 8990);
		
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		
		out.write("capture-500,500,500,500".getBytes());
		int len = new DataInputStream(in).readInt();
		
		System.out.println(len);
		
		byte[] buffer = new byte[len];
		int size = in.read(buffer);
		
		System.out.println(size);
		
		FileOutputStream fileOut = new FileOutputStream(new File("d:/a.png"));
		fileOut.write(buffer);
		fileOut.close();
		
		socket.close();
	}

}
