package org.suren.littlebird.gui.menu;

import org.suren.littlebird.log.ArchLogger;

public abstract class ArchMenu<Cfg>
{
	protected ArchLogger logger = ArchLogger.getInstance();
	
	protected abstract boolean saveCfg(Cfg cfgObj);
	
	protected abstract Cfg loadCfg();
}
