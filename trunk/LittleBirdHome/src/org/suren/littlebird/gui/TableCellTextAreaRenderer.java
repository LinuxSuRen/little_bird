package org.suren.littlebird.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import sun.swing.DefaultLookup;

public class TableCellTextAreaRenderer extends JTextArea implements
		TableCellRenderer
{
	private static final long	serialVersionUID	= 1L;
	
    private Color unselectedForeground; 
    private Color unselectedBackground; 

	public TableCellTextAreaRenderer() throws HeadlessException
	{
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		String text = value != null ? value.toString() : "";
		setText(text);
		
		int height = getPreferredSize().height;
		if(table.getRowHeight(row) != height)
		{
			table.setRowHeight(row, height);
		}
		
        Color fg = null;
        Color bg = null;

        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column) {

            fg = DefaultLookup.getColor(this, ui, "Table.dropCellForeground");
            bg = DefaultLookup.getColor(this, ui, "Table.dropCellBackground");

            isSelected = true;
        }

		if (isSelected)
		{
			super.setForeground(fg == null ? table.getSelectionForeground()
					: fg);
			super.setBackground(bg == null ? table.getSelectionBackground()
					: bg);
		}
		else
		{
			Color background = unselectedBackground != null ? unselectedBackground
					: table.getBackground();
			if (background == null
					|| background instanceof javax.swing.plaf.UIResource)
			{
				Color alternateColor = DefaultLookup.getColor(this, ui,
						"Table.alternateRowColor");
				if (alternateColor != null && row % 2 == 0)
					background = alternateColor;
			}
			super.setForeground(unselectedForeground != null ? unselectedForeground
					: table.getForeground());
			super.setBackground(background);
		}

		return this;
	}
}
