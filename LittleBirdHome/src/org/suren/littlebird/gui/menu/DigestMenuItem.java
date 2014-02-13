package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.security.DigestServer;
import org.suren.littlebird.security.DigestTask;

@Menu(displayName = "Digest", parentMenu = SecurityMenu.class, index = 0)
public class DigestMenuItem extends ArchMenu
{
	private JPanel panel = null;
	private DigestServer digestServer;
	
	private JFileChooser fileChooser;
	
	private JToolBar toolBar;
	private JButton startBut;
	private JButton addBut;
	private JButton delBut;
	private JCheckBox updateEnableCheck;
	private JTable centerTable;
	
	private Timer updateTimer;
	
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			MainFrame main = MainFrame.getInstance();
			if(panel == null)
			{
				panel = new JPanel();
				panel.setBackground(Color.BLACK);
				panel.setLayout(new BorderLayout());
				main.getContentPanel().add("Digest", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Digest");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
		if(digestServer != null)
		{
			return;
		}
		
		digestServer = DigestServer.getInstance();
		digestServer.init(10, 20);
		digestServer.start();
		
		toolBar = new JToolBar();
		fillToolBar(toolBar);
		
		centerTable = new JTable();
		JScrollPane scrollPane = new JScrollPane(centerTable);
		
		setTableHeader();
		setTableActin();
		
		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		updateTimer = new Timer();
		
		scrollPane.setDropTarget(new DropTarget(){

			private static final long	serialVersionUID	= 1415079189100811337L;

			@Override
			public synchronized void drop(DropTargetDropEvent drop)
			{
				drop.acceptDrop(DnDConstants.ACTION_REFERENCE);
				Transferable transferable = drop.getTransferable();
				
				try
				{
					Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
					List<File> result = null;
					
					if(data == null || !(data instanceof List))
					{
						return;
					}
					
					result = (List<File>)data;
					pushFiles((File[]) result.toArray());
				}
				catch (UnsupportedFlavorException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private void fillToolBar(JToolBar toolBar)
	{
		if(toolBar == null)
		{
			return;
		}
		
		startBut = new JButton("start");
		startBut.addActionListener(startDigest);
		
		addBut = new JButton("add");
		addBut.addActionListener(addFileRecord);
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		
		delBut = new JButton("del");
		delBut.addActionListener(delFileRecord);
		
		updateEnableCheck = new JCheckBox("update");
		updateEnableCheck.addItemListener(updateChange);
		
		toolBar.add(startBut);
		toolBar.add(addBut);
		toolBar.add(delBut);
		toolBar.add(updateEnableCheck);
	}

	private final String HEAD_PATH = "path";
	private final String HEAD_SIZE = "size";
	private final String HEAD_TYPE = "type";
	private final String HEAD_LEVEL = "level";
	private final String HEAD_TIME = "time";
	private final String HEAD_RESULT = "result";
	
	private void setTableHeader()
	{
		TableModel model = new DefaultTableModel(new String[]{
				HEAD_PATH,
				HEAD_SIZE,
				HEAD_TYPE,
				HEAD_LEVEL,
				HEAD_TIME,
				HEAD_RESULT
		}, 0);
		
		centerTable.setModel(model);
	}

	private void setTableActin()
	{
	}
	
	private int getColumnByName(TableModel model, String name)
	{
		if(model == null || name == null)
		{
			return -1;
		}
		
		int count = model.getColumnCount();
		for(int i = 0; i < count; i++)
		{
			String colName = model.getColumnName(i);
			
			if(name.equals(colName))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getValueAt(TableModel model, int row, String colName, T defObj)
	{
		if(model == null)
		{
			return defObj;
		}
		
		int colCount = model.getColumnCount();
		int rowCount = model.getRowCount();
		int column = getColumnByName(model, colName);
		
		if(row < 0 || row > rowCount || column < 0 || column > colCount)
		{
			return defObj;
		}
		
		Object value = model.getValueAt(row, column);
		if(value == null)
		{
			return defObj;
		}
		
		try
		{
			return (T)value;
		}
		catch(ClassCastException e)
		{
			return defObj;
		}
	}
	
	private String getValueAt(TableModel model, int row, String colName)
	{
		return getValueAt(model, row, colName, "");
	}
	
	private boolean setValueAt(TableModel model, int row, String colName, Object value)
	{
		if(model == null)
		{
			return false;
		}
		
		int colCount = model.getColumnCount();
		int rowCount = model.getRowCount();
		int column = getColumnByName(model, colName);
		
		if(row < 0 || row > rowCount || column < 0 || column > colCount)
		{
			return false;
		}
		
		model.setValueAt(value, row, column);
		
		return true;
	}
	
	class PerformEvent
	{
		private JTable table;
		
		public PerformEvent(JTable table)
		{
			this.table = table;
		}

		public JTable getTable()
		{
			return table;
		}

		public void setTable(JTable table)
		{
			this.table = table;
		}
	}
	
	class PerformButton extends JButton
	{
		private static final long	serialVersionUID	= -5788770853072873062L;
		
		public PerformButton(String text)
		{
			super(text);
		}

		public void actionPerformed(PerformEvent event)
		{
			JTable table = event.getTable();
			if(table == null)
			{
				return;
			}
			
			int row = table.getSelectedRow();
			if(row == -1)
			{
				return;
			}
			
			System.out.println(row);
			
			Object value = table.getModel().getValueAt(row, 0);
			if(value == null)
			{
				return;
			}
			
			DigestTask task = new DigestTask();
			task.setType(task.DIG_MD5);
			task.setFile(new File(value.toString()));
			
			digestServer.addTask(task);
		}
	}
	
	private ActionListener startDigest = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int[] rows= centerTable.getSelectedRows();
			TableModel model = centerTable.getModel();
			
			for(int row : rows)
			{
				String path = getValueAt(model, row, HEAD_PATH);
				
				DigestTask task = new DigestTask();
				task.setType(task.DIG_MD5);
				task.setFile(new File(path));
				
				digestServer.addTask(task);
				
				model.setValueAt(task, row, 5);
			}
		}
	};
	
	private ActionListener addFileRecord = new ActionListener()
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			{
				return;
			}
			
			File[] files = fileChooser.getSelectedFiles();
			pushFiles(files);
		}
	};
	
	private void pushFiles(File[] files)
	{
		TableModel tbModel = centerTable.getModel();
		DefaultTableModel model = null;
		if(tbModel instanceof DefaultTableModel)
		{
			model = (DefaultTableModel) tbModel;
		}
		else
		{
			return;
		}
		
		pushFiles(model, files);
	}
	
	private void pushFiles(DefaultTableModel model, File[] files)
	{
		pushFiles(model, files, true);
	}
	
	private void pushFiles(DefaultTableModel model, File[] files, boolean onlyFile)
	{
		if(files == null)
		{
			return;
		}
		
		for(File file : files)
		{
			if(onlyFile && !file.isFile())
			{
				continue;
			}
			
			Vector<Object> rowData = new Vector<Object>();
			rowData.add(file.getAbsolutePath());
			rowData.add(file.length());
			
			model.addRow(rowData);
		}
	}
	
	private ActionListener delFileRecord = new ActionListener()
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int[] rows= centerTable.getSelectedRows();
			TableModel tbModel = centerTable.getModel();
			DefaultTableModel model = null;
			if(tbModel instanceof DefaultTableModel)
			{
				model = (DefaultTableModel) tbModel;
			}
			else
			{
				return;
			}
			
			Arrays.sort(rows);
			int count = rows.length;
			
			for(int i = count - 1; i >= 0; i--)
			{
				model.removeRow(rows[i]);
			}
		}
	};
	
	private ItemListener updateChange = new ItemListener()
	{

		@Override
		public void itemStateChanged(ItemEvent e)
		{
			Object srcObj = e.getSource();
			if(srcObj instanceof JCheckBox)
			{
				JCheckBox updateCheck = (JCheckBox) srcObj;
				
				if(updateCheck.isSelected())
				{
					updateTimer.scheduleAtFixedRate(new TimerTask()
					{
						
						@Override
						public void run()
						{
							taskUpdate();
						}
					}, 500, 1000);
				}
			}
		}
	};

	private void taskUpdate()
	{
		TableModel tbModel = centerTable.getModel();
		int rowCount = tbModel.getRowCount();
		
		for(int i = 0; i < rowCount; i++)
		{
			DigestTask task = null;
			Object taskObj = getValueAt(tbModel, i, HEAD_RESULT, null);
			if(taskObj instanceof DigestTask)
			{
				task = (DigestTask) taskObj;
			}
			
			if(task == null)
			{
				continue;
			}
			
			int level = task.getLevel();
			
			setValueAt(tbModel, i, HEAD_RESULT, task);
			setValueAt(tbModel, i, HEAD_TYPE, task.getType());
			setValueAt(tbModel, i, HEAD_LEVEL, level);
			
			if(level == 100)
			{
				setValueAt(tbModel, i, HEAD_RESULT, task.getResult());
				setValueAt(tbModel, i, HEAD_TIME, task.getEndTime() - task.getStartTime());
			}
			else
			{
				setValueAt(tbModel, i, HEAD_TIME, System.currentTimeMillis() - task.getStartTime());
			}
		}
	}
}
