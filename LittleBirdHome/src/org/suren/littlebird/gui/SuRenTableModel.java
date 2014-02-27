package org.suren.littlebird.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class SuRenTableModel extends DefaultTableModel
{

	private static final long	serialVersionUID	= 1L;
	
	private boolean readOnly = true;
	private HashSet<Integer> readOnlyColumns = new HashSet<Integer>();

	@Override
	public boolean isCellEditable(int row, int column)
	{
		if(readOnly)
		{
			return false;
		}
		
		boolean readOnly = readOnlyColumns.contains(column);
		
		return !readOnly;
	}
	
	public boolean addReadOnlyColumn(int column)
	{
		return readOnlyColumns.add(column);
	}
	
	public boolean delReadOnlyColumn(int column)
	{
		return readOnlyColumns.remove(column);
	}
	
	public void addRow(Map<Object, Object> row, Object defObj)
	{
		if(row == null || row.size() == 0)
		{
			return;
		}
		
		addRow(new Vector<Object>());
		int rowIndex = getRowCount() - 1;
		Map<String, Integer> columnNames = getColumnMap();
		
		Set<Object> keys = row.keySet();
		for(Object key : keys)
		{
			Integer index = columnNames.get(key);
			if(index == null)
			{
				continue;
			}
			
			Object value = row.get(key);
			setValueAt(value == null ? defObj : value, rowIndex, index.intValue());
		}
	}
	
	public Object getValueAt(int row, String columnName)
	{
		int rowCount = getRowCount();
		if(row < 0 || row >= rowCount)
		{
			return null;
		}
		
		Map<String, Integer> columnNames = getColumnMap();
		Integer columnIndex = columnNames.get(columnName);
		if(columnIndex == null)
		{
			return null;
		}
		
		return getValueAt(row, columnIndex.intValue());
	}
	
	public Map<String, Integer> getColumnMap()
	{
		int count = getColumnCount();
		Map<String, Integer> columnNames = new HashMap<String, Integer>();
		for(int i = 0; i < count; i++)
		{
			columnNames.put(getColumnName(i), i);
		}
		
		return columnNames;
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if(getRowCount() > 0)
		{
			Object value = getValueAt(0, columnIndex);
			
			if(value instanceof Number)
			{
				return Number.class;
			}
			else if(value instanceof String)
			{
				return String.class;
			}
		}
		
		return super.getColumnClass(columnIndex);
	}
}
