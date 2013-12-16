package org.suren.littlebird.test;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ToolkitTest {

	/**
	 * @param args
	 * @throws AWTException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws AWTException, FileNotFoundException, IOException {
		long begin = System.currentTimeMillis();
		Robot robot = new Robot();
		
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle rectangle = device.getDefaultConfiguration().getBounds();
		
		rectangle.height = 500;
		rectangle.width = 500;
		
		for(int i = 0; i < 10; i+= 10)
		{
			rectangle.x = i;
			rectangle.y = i;
			
			BufferedImage buf = robot.createScreenCapture(rectangle);
			
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(1000){
				
				private int total = 0;

				@Override
				public synchronized void write(byte[] b, int off, int len) {
					total += len;
//					System.out.println(off + "-------" + len + "-------" + total);
					super.write(b, off, len);
				}

//				@Override
//				public void write(byte[] b, int off, int len)
//						throws IOException {
//					super.write(b, off, len);
//				}
			};
			
			String[] imgType = {"png", "gif", "jpg", "bmp"};
			
			for(String type : imgType)
			{
				ImageIO.write(buf, "gif", byteArrayOut);
				byte[] bytes = byteArrayOut.toByteArray();
				System.out.println(type + " -- bytes : " + bytes.length);
			}
		}
		
		System.out.println(System.currentTimeMillis() - begin);
	}

}
