package org.suren.littlebird;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.swing.UnsupportedLookAndFeelException;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Publish;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.server.RmiServer;

public class Launcher
{

	/**
	 * @param args
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 * @throws RemoteException 
	 * @throws NotCompliantMBeanException 
	 * @throws MBeanRegistrationException 
	 * @throws InstanceAlreadyExistsException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, RemoteException
	{
		Launcher launcher = new Launcher();
		launcher.splash();
		launcher.init();
		
		MainFrame main = MainFrame.getInstance();
		
		main.setVisible(true);
		
		new Thread(new RmiServer()).start();
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName objectName = new ObjectName("org.suren.littlebird:type=TestHello");
		
		TestHello test = new TestHello();
		mBeanServer.registerMBean(test, objectName);
		mBeanServer.registerMBean(new DyMBeanTest(), new ObjectName("org.suren.littlebird:type=DyMBeanTest"));
	}

	private void splash()
	{
		new Thread("launcher splash"){

			@Override
			public void run()
			{
				SplashScreen splashScreen = SplashScreen.getSplashScreen();
				Graphics2D g = splashScreen.createGraphics();
				
				g.setColor(Color.RED);
				g.drawString("hello suren", 100, 100);
				
				splashScreen.update();
			}
		}.start();
	}

	private void init()
	{
		ResourceLoader loader = ResourceLoader.getInstance();
		
		loader.discover(Menu.class, Publish.class);
	}

}
