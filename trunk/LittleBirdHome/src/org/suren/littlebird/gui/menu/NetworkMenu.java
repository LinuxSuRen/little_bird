package org.suren.littlebird.gui.menu;

import org.suren.littlebird.annotation.Menu;

@Menu(displayName = "Network", parentMenu = Object.class, index = 5)
public class NetworkMenu extends ArchMenu<Object>
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
