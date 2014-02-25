package org.suren.littlebird.gui;

import java.util.Vector;

import javax.swing.JTable;

public class SuRenTable extends JTable
{
	private static final long	serialVersionUID	= 1L;
	
	private SuRenTableModel model;
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
		
		setAutoCreateRowSorter(true);
	}
	
	public void setHeaders(String ... headers)
	{
		if(headers == null)
		{
			return;
		}
		
		int len = headers.length;
		Vector<String> headersV = new Vector<String>(len);
		for(String header : headers)
		{
			headersV.addElement(header);
		}
		
		setHeaders(headersV);
	}
	
	public void setHeaders(Vector<String> headers)
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
