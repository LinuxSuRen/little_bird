package org.suren.littlebird.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComboBox;

public class SuRenComboBox extends JComboBox implements MouseListener
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

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent)
	{
		int clickCount = mouseEvent.getClickCount();
		if(clickCount == 2)
		{
			this.setEditable(true);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent mouseEvent)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent mouseEvent)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent mouseEvent)
	{
	}

}
