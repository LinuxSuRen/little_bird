package org.suren.littlebird.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;

import org.suren.littlebird.util.NumberComparator;

public class SuRenTable extends JTable
{
	private static final long	serialVersionUID	= 1L;
	
	private SuRenTableModel model;
	private SuRenTableRowSorter rowSorter;
	private SuRenTableCellRenderer cellRenderer = new SuRenTableCellRenderer();
	private Map<String, Object> data = new HashMap<String, Object>();
	
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
		int index = getModel().getColumnIndex(columnName);
		
		if(index != -1)
		{
			return getValueAt(row, index);
		}
		else
		{
			return null;
		}
	}

	public Map<String, Object> getData()
	{
		return data;
	}

	public void addData(Map<String, Object> data)
	{
		this.data.putAll(data);
	}
	
	public void addData(String key, Object value)
	{
		data.put(key, value);
	}
	
	public void rowsSelect(int[] rows)
	{
		if(rows != null && rows.length > 0)
		{
			Arrays.sort(rows);
			int size = rows.length;
			int index = 0;
			int firstRow = rows[0];
			int lastRow = firstRow;
			
			for(; index < size; index++)
			{
				if(lastRow + 1 == rows[index])
				{
					lastRow++;
				}
				else
				{
					setRowSelectionInterval(firstRow, lastRow);
					
					firstRow = rows[index];
					lastRow = firstRow;
				}
			}
			
			setRowSelectionInterval(firstRow, lastRow);
		}
	}
}
