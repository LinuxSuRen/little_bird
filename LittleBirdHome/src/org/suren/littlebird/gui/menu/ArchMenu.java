package org.suren.littlebird.gui.menu;

import javax.swing.JLabel;

import org.suren.littlebird.log.ArchLogger;

public abstract class ArchMenu<Cfg>
{
	private JLabel statusLabel;
	
	protected ArchLogger logger = ArchLogger.getInstance();
	
	protected abstract boolean saveCfg(Cfg cfgObj);
	
	protected abstract Cfg loadCfg();
	
	protected void updateStatus(Object ... status)
	{
		if(status == null || getStatusLabel() == null)
		{
			return;
		}
		
		StringBuilder buffer = new StringBuilder("status:");
		
		for(Object obj : status)
		{
			buffer.append(obj == null ? "" : obj.toString());
		}
		
		getStatusLabel().setText(buffer.toString());
		getStatusLabel().updateUI();
	}

	public JLabel getStatusLabel()
	{
		return statusLabel;
	}

	public void setStatusLabel(JLabel statusLabel)
	{
		this.statusLabel = statusLabel;
	}
}
