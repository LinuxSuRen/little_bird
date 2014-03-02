package org.suren.littlebird.gui.menu;

import org.suren.littlebird.annotation.Menu;

@Menu(displayName = "Security", parentMenu = Object.class, index = 2)
public class SecurityMenu extends ArchMenu<Object>
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
