package org.suren.littlebird.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class SuRenTableCellRenderer extends DefaultTableCellRenderer
{

	private static final long	serialVersionUID	= 1L;
	
	private List<Integer> columnWidths = new ArrayList<Integer>();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component component = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		
		int columnCount = table.getColumnCount();
		if(columnWidths.size() < columnCount)
		{
			int gap = columnCount - columnWidths.size();
			for(int i = 0; i < gap; i++)
			{
				columnWidths.add(10);
			}
		}
		
		Dimension preferredSize = component.getPreferredSize();
		if(preferredSize.width > columnWidths.get(column))
		{
			columnWidths.set(column, preferredSize.width);
		}
		
		TableColumn columnObj = table.getColumnModel().getColumn(column);
		int width = columnObj.getMinWidth();
		if(width != columnWidths.get(column))
		{
			columnObj.setMinWidth(columnWidths.get(column));
		}
		
		if(value != null && !"".equals(value.toString()) &&
				table.getRowHeight(row) < preferredSize.height)
		{
			table.setRowHeight(row, preferredSize.height);
		}
		
		return component;
	}

}
