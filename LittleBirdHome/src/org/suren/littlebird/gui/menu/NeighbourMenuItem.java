package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;

@Menu(displayName = "Neighbour", parentMenu = NetworkMenu.class, index = 1,
	keyCode = KeyEvent.VK_N, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class NeighbourMenuItem extends ArchMenu<Object>
{
	protected JPanel panel;
	private JToolBar controlbar;
	
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			MainFrame main = MainFrame.getInstance();
			if(panel == null)
			{
				panel = new JPanel();
				panel.setLayout(new BorderLayout());
				main.getContentPanel().add("Neighbour", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Neighbour");
			
			main.reDrawPanel();
		}
	};

	protected void init()
	{
		if(controlbar != null)
		{
			return;
		}
		
		createControlBar();
		
		createCenterPanel();
	}

	private void createControlBar()
	{
	}

	private void createCenterPanel()
	{
		controlbar = new JToolBar("Control");
		
		JButton startBut = new JButton("Start");
		
		controlbar.add(startBut);
		
		panel.add(controlbar, BorderLayout.NORTH);
	}

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
