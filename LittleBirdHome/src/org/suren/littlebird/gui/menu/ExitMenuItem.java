package org.suren.littlebird.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;

@Menu(displayName = "Exit", parentMenu = FileMenu.class, index = 1)
public class ExitMenuItem extends ArchMenu<Object>
{
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			logger.debug("ExitMenuItem");
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
