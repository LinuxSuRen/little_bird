package org.suren.littlebird.gui.menu;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;

@Menu(displayName = "Digest", parentMenu = SecurityMenu.class, index = 0)
public class DigestMenuItem extends ArchMenu
{
	private JPanel panel = null;
	
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
				panel.setBackground(Color.BLACK);
				main.getContentPanel().add("Digest", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Digest");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
	}
}
