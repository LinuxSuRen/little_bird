package org.suren.littlebird.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class SuRenFilterBox<E> extends JComboBox<E> implements KeyListener
{
	private static final long	serialVersionUID	= 1L;
	private List<E> sourceList = new ArrayList<E>();

	public SuRenFilterBox()
	{
		super();
		
		filterListen();
	}

	public SuRenFilterBox(ComboBoxModel<E> aModel)
	{
		super(aModel);
		
		filterListen();
	}

	public SuRenFilterBox(E[] items)
	{
		super(items);
		
		filterListen();
	}

	public SuRenFilterBox(Vector<E> items)
	{
		super(items);
		
		filterListen();
	}
	
	private void filterListen()
	{
		this.getEditor().getEditorComponent().addKeyListener(this);
	}
	
	public void setSource(List<E> sourceList)
	{
		if(sourceList == null)
		{
			return;
		}
		
		this.setEditable(true);
		this.sourceList = sourceList;
	}

	public void addUniItem(E item)
	{
		int count = getItemCount();
		for(int i = 0; i < count; i++)
		{
			if(getItemAt(i).equals(item))
			{
				return;
			}
		}
		
		this.addItem(item);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		Object selectedItem = this.getEditor().getItem();
		String filter;
		int code = e.getKeyCode();
		if(selectedItem == null || (filter = selectedItem.toString()) == null)
		{
			return;
		}
		
		if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE)
		{
			this.hidePopup();
			
			return;
		}
		
		if(code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN)
		{
			return;
		}

		filter = (".*" + filter + ".*");
		
		Set<E> tmpSet = new HashSet<E>();
		for(E item : sourceList)
		{
			if(item == null)
			{
				continue;
			}
			
			if(item.toString().matches(filter))
			{
				tmpSet.add(item);
			}
		}
		
		this.removeAllItems();
		
		tmpSet.remove(selectedItem);
		this.addItem((E) selectedItem);
		
		for(E item : tmpSet)
		{
			this.addItem(item);
		}
		this.showPopup();
		this.setSelectedItem(selectedItem);
	}
}
