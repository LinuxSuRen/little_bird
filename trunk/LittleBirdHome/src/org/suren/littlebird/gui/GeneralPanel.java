package org.suren.littlebird.gui;

import javax.swing.JPanel;

public class GeneralPanel<T> extends JPanel
{

	private static final long	serialVersionUID	= -8216800621148768066L;
	
	private T dataObject;

	public T getDataObject()
	{
		return dataObject;
	}

	public void setDataObject(T dataObject)
	{
		this.dataObject = dataObject;
	}

}
