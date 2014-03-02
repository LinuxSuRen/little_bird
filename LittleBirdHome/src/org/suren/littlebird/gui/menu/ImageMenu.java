package org.suren.littlebird.gui.menu;

import org.suren.littlebird.annotation.Menu;

@Menu(displayName = "Image", parentMenu = Object.class, index = 3)
public class ImageMenu extends ArchMenu<Object>
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
