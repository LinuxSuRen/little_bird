package org.suren.littlebird.gui.menu;

import org.suren.littlebird.annotation.Menu;

@Menu(displayName = "Server", parentMenu = Object.class, index = 1)
public class ServerMenu extends ArchMenu<Object>
{

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
