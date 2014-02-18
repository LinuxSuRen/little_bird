package org.suren.littlebird;

import javax.swing.UnsupportedLookAndFeelException;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Publish;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.server.RmiServer;

public class Launcher
{

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args)
	{
		Launcher launcher = new Launcher();
		launcher.init();
		
		MainFrame main = MainFrame.getInstance();
		
		main.setVisible(true);
		
		new Thread(new RmiServer()).start();
	}

	private void init()
	{
		ResourceLoader loader = ResourceLoader.getInstance();
		
		loader.discover(Menu.class, Publish.class);
	}

}
