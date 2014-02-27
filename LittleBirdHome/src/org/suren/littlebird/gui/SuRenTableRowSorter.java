package org.suren.littlebird.gui;

import java.util.Comparator;

import javax.swing.table.TableRowSorter;

import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.util.NumberComparator;

public class SuRenTableRowSorter extends TableRowSorter<SuRenTableModel>
{

	public SuRenTableRowSorter()
	{
		super();
	}

	public SuRenTableRowSorter(SuRenTableModel model)
	{
		super(model);
	}

	@Override
	public Comparator<?> getComparator(int column)
	{
		Class<?> columnCls = getModel().getColumnClass(column);
		
		if(columnCls == Number.class)
		{
			return NumberComparator.getInstance();
		}
		
		return super.getComparator(column);
	}

}
