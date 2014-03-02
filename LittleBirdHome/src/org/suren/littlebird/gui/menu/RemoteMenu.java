package org.suren.littlebird.gui.menu;

import org.suren.littlebird.annotation.Menu;

@Menu(displayName = "Remote", parentMenu = Object.class, index = 4)
public class RemoteMenu extends ArchMenu<Object>
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
