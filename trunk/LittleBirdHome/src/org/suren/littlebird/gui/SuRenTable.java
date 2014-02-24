package org.suren.littlebird.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SuRenTable extends JTable
{
	private static final long	serialVersionUID	= 1L;
	
	private DefaultTableModel model;
	
	public SuRenTable()
	{
		super();
		
		if(getModel() instanceof DefaultTableModel)
		{
			model = (DefaultTableModel) getModel();
		}
		else
		{
			model = new DefaultTableModel();
			setModel(model);
		}
	}

}
