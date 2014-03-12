package org.suren.littlebird.gui;

import javax.swing.JComboBox;

public class SuRenComboBox extends JComboBox
{

	private static final long	serialVersionUID	= 1L;

	public void addUniItem(Object item)
	{
		int count = getItemCount();
		for(int i = 0; i < count; i++)
		{
			if(getItemAt(i).equals(item))
			{
				return;
			}
		}
		
		super.addItem(item);
	}

}
