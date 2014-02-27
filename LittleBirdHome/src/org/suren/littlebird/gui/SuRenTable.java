package org.suren.littlebird.gui;

import java.util.Vector;

import javax.swing.JTable;

import org.suren.littlebird.util.NumberComparator;

public class SuRenTable extends JTable
{
	private static final long	serialVersionUID	= 1L;
	
	private SuRenTableModel model;
	private SuRenTableRowSorter rowSorter;
	private SuRenTableCellRenderer cellRenderer = new SuRenTableCellRenderer();
	
	public SuRenTable()
	{
		super();
		
		if(getModel() instanceof SuRenTableModel)
		{
			model = (SuRenTableModel) getModel();
		}
		else
		{
			model = new SuRenTableModel();
			setModel(model);
		}
		
		rowSorter = new SuRenTableRowSorter(getModel());
		setRowSorter(rowSorter);
		firePropertyChange("autoCreateRowSorter", false, true);
	}
	
	public void setHeaders(Object ... headers)
	{
		if(headers == null)
		{
			return;
		}
		
		int len = headers.length;
		Vector<Object> headersV = new Vector<Object>(len);
		for(Object header : headers)
		{
			headersV.addElement(header);
		}
		
		setHeaders(headersV);
	}
	
	public void setHeaders(Vector<Object> headers)
	{
		if(headers == null)
		{
			return;
		}

		model.setDataVector(new Vector<String>(0), headers);
		
		int columnCount = getColumnCount();
		for(int i = 0; i < columnCount; i++)
		{
			getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
		}
	}
	
	public void setColumnSorterClass(int index, Class<?> clazz)
	{
		int columnCount = getColumnCount();
		if(index < 0 || index >= columnCount)
		{
			return;
		}
		
		if(clazz == Number.class)
		{
			rowSorter.setComparator(index, NumberComparator.getInstance());
		}
	}

	@Override
	public SuRenTableModel getModel()
	{
		return model;
	}
	
	public Object getValueAt(int row, String columnName)
	{
		return getModel().getValueAt(row, columnName);
	}
}
