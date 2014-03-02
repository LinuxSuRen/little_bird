package org.suren.littlebird.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

@Menu(displayName = "About", parentMenu = HelpMenu.class)
public class AboutMenuItem extends ArchMenu<Object>
{
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				UIManager.setLookAndFeel(new WindowsLookAndFeel());
				SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
				MainFrame.getInstance().validate();
			}
			catch (UnsupportedLookAndFeelException e1)
			{
				e1.printStackTrace();
			}
		}
	};

	@Override
	protected boolean saveCfg(Object cfgObj)
	{
		return false;
	}

	@Override
	protected Object loadCfg()
	{
		return null;
	}
}
