package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.FocusAndSelectListener;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.TableCellTextAreaRenderer;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.SuRenBundle;
import org.suren.littlebird.setting.OsgiMgrSetting;
import org.suren.littlebird.setting.SettingUtil;

@Menu(displayName = "Osgi", parentMenu = RemoteMenu.class, index = 1)
public class OsgiMenuItem extends ArchMenu
{
	private final String	HEAD_ID			= "id";
	private final String	HEAD_NAME		= "name";
	private final String	HEAD_VERSION	= "version";
	private final String	HEAD_STATE		= "state";
	private final String	HEAD_VALUE		= "value";
	
	private final int			Start		= 0x1;
	private final int			Stop		= 0x2;
	private final int			Uninstall	= 0x3;
	private final int			Update		= 0x4;

	private JPanel				panel		= null;
	
	private Setting setting = null;
	
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
				panel.setLayout(new BorderLayout());
				main.getContentPanel().add("Osgi", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Osgi");
			
			main.reDrawPanel();
		}
	};

	protected void init()
	{
		if(setting != null)
		{
			return;
		}
		
		setting = new Setting();
		
		JTable table = new JTable();
		createCenter(table);
		createToolBar(table);
	}
	
	private void createToolBar(final JTable table)
	{
		final Set<String> data = new HashSet<String>();
		final JToolBar controlBar = new JToolBar();
		final JToolBar installBar = createInstallBar(data);
		final JToolBar settingBar = createSettingBar();
		
		final JButton reloadBut = new JButton("Reload");
		JButton startBut = new JButton("Start");
		JButton stopBut = new JButton("Stop");
		JButton updateBut = new JButton("Update");
		JButton installBut = new JButton("Install");
		JButton uninstallBut = new JButton("Uninstall");
		final JComboBox filterBox = new JComboBox();
		JButton settingBut = new JButton("Setting");
		
		reloadBut.setMnemonic('r');
		startBut.setMnemonic('s');
		stopBut.setMnemonic('t');
		updateBut.setMnemonic('e');
		installBut.setMnemonic('i');
		uninstallBut.setMnemonic('u');
		settingBut.setMnemonic('g');
		filterBox.setEditable(true);
		filterBox.setToolTipText("Ctrl+E");
		filterBox.registerKeyboardAction(new FocusAndSelectListener(),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		controlBar.add(reloadBut);
		controlBar.add(startBut);
		controlBar.add(stopBut);
		controlBar.add(updateBut);
		controlBar.add(installBut);
		controlBar.add(uninstallBut);
		controlBar.add(filterBox);
		controlBar.add(settingBut);
		
		reloadBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object item = filterBox.getSelectedItem();
				
				if(item == null)
				{
					loadOsgiInfo(table);
				}
				else
				{
					loadOsgiInfo(table, item.toString());
				}
			}
		});
		
		startBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(controlOsgiBundle(table, Start) > 0)
				{
					reloadBut.doClick();
				}
			}
		});
		
		stopBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(controlOsgiBundle(table, Stop) > 0)
				{
					reloadBut.doClick();
				}
			}
		});
		
		updateBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(controlOsgiBundle(table, Update) > 0)
				{
					reloadBut.doClick();
				}
			}
		});
		
		installBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean visible = installBar.isVisible();

				if(visible && installBundle(data) > 0)
				{
					reloadBut.doClick();
				}
				
				installBar.setVisible(!visible);
			}
		});
		
		uninstallBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(controlOsgiBundle(table, Uninstall) > 0)
				{
					reloadBut.doClick();
				}
			}
		});
		
		filterBox.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				String cmd = e.getActionCommand();
				
				if("comboBoxEdited".equals(cmd) && source instanceof JComboBox)
				{
					JComboBox filter = (JComboBox) source;
					Object item = filter.getSelectedItem();
					
					loadOsgiInfo(table, item.toString());
					
					if(table.getRowCount() > 0)
					{
						collectForOnly(filter, item);
					}
				}
			}
		});
		
		settingBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				settingBar.setVisible(!settingBar.isVisible());
			}
		});
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		controlPanel.add(controlBar, BorderLayout.NORTH);
		controlPanel.add(installBar, BorderLayout.CENTER);
		controlPanel.add(settingBar, BorderLayout.SOUTH);
		
		panel.add(controlPanel, BorderLayout.NORTH);
	}
	
	private JToolBar createSettingBar()
	{
		final JToolBar settingBar = new JToolBar();
		GridLayout gridLayout = new GridLayout();
		gridLayout.setColumns(2);
		settingBar.setVisible(false);
		settingBar.setLayout(gridLayout);

		final JTextField urlField = new JTextField();
		final JTextField portField = new JTextField();
		JButton saveBut = new JButton("save");
		
		settingBar.add(new JLabel("url"));
		settingBar.add(urlField);
		settingBar.add(new JLabel("port"));
		settingBar.add(portField);
//		settingBar.addSeparator();
		settingBar.add(saveBut);
		
		OsgiMgrSetting data = new SettingUtil<OsgiMgrSetting>().load(
				OsgiMgrSetting.class, "d:/a.xml");
		
		if(data != null)
		{
			urlField.setText(data.getHost());
			portField.setText(String.valueOf(data.getPort()));
			
			setting.setUrl(data.getHost());
			setting.setPort(data.getPort());
		}
		
		saveBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String url = urlField.getText();
				String strPort = portField.getText();
				int port = -1;
				
				try
				{
					port = Integer.parseInt(strPort);
				}
				catch(NumberFormatException exc)
				{
				}
				
				if(url.equals("") || port <= 0 || port >= 65536)
				{
					return;
				}

				setting.setUrl(url);
				setting.setPort(port);
				
				settingBar.setVisible(false);
				
				save(setting);
			}
			
			private boolean save(Setting setting)
			{
				if(setting == null)
				{
					return false;
				}
				
				OsgiMgrSetting osgiSetting = new OsgiMgrSetting();
				osgiSetting.setHost(setting.getUrl());
				osgiSetting.setPort(setting.getPort());
				
				return new SettingUtil<OsgiMgrSetting>().save(OsgiMgrSetting.class,
						osgiSetting, "d:/a.xml");
			}
		});
		
		return settingBar;
	}

	private JToolBar createInstallBar(final Set<String> data)
	{
		final JToolBar installBar = new JToolBar();
		installBar.setLayout(new BoxLayout(installBar, BoxLayout.Y_AXIS));
		installBar.setVisible(false);

		final JCheckBox remote = new JCheckBox("remote");
		JTextField pathField = new JTextField();
		final DefaultListModel listModel = new DefaultListModel();
		JList remotePathList = new JList(listModel);
		remotePathList.setVisibleRowCount(5);
		
		pathField.setToolTipText("Ctrl+L");
		pathField.registerKeyboardAction(new FocusAndSelectListener(),
				KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		remote.addItemListener(new ItemListener()
		{
			
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				int state = e.getStateChange();
				Enumeration<?> items = listModel.elements();
				
				if(state == ItemEvent.SELECTED)
				{
					while(items.hasMoreElements())
					{
						data.add(items.nextElement().toString());
					}
				}
				else if(state == ItemEvent.DESELECTED)
				{
					while(items.hasMoreElements())
					{
						data.remove(items.nextElement().toString());
					}
				}
			}
		});
		
		pathField.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String cmd = e.getActionCommand();
				
				if(!listModel.contains(cmd))
				{
					listModel.addElement(cmd);
					
				}
				
				if(remote.isSelected())
				{
					data.add(cmd);
				}
			}
		});
		
		remotePathList.addKeyListener(new KeyAdapter()
		{

			@Override
			public void keyReleased(KeyEvent e)
			{
				int code = e.getKeyCode();
				Object source = e.getSource();
				
				if(code == KeyEvent.VK_DELETE && source instanceof JList)
				{
					JList remotePathList = (JList) source;
					int index = remotePathList.getSelectedIndex();
					
					if(index != -1)
					{
						listModel.remove(index);
					}
				}
			}
		});
		
		JPanel remoteTypePanel = new JPanel();
		remoteTypePanel.add(remote);
		
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
		pathPanel.add(pathField);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		listPanel.add(remotePathList);
		pathPanel.add(listPanel);
		
		JPanel remotePanel = new JPanel();
		remotePanel.setLayout(new BoxLayout(remotePanel, BoxLayout.X_AXIS));
		remotePanel.add(remoteTypePanel);
		remotePanel.add(pathPanel);
		
		JButton cancelBut = new JButton("Cancel");
		JButton clearBut = new JButton("Clear");
		JPanel cancelPanel = new JPanel();
		cancelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cancelPanel.add(clearBut);
		cancelPanel.add(cancelBut);
		
		clearBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				listModel.clear();
			}
		});
		
		cancelBut.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				installBar.setVisible(false);
			}
		});
		
		installBar.add(remotePanel);
		installBar.add(cancelPanel);
		
		return installBar;
	}

	private void collectForOnly(JComboBox box, Object item)
	{
		if(box == null)
		{
			return;
		}
		
		int count = box.getItemCount();
		boolean notExists = true;
		for(int i = 0; i < count; i++)
		{
			if(box.getItemAt(i).equals(item))
			{
				notExists = false;
			}
		}
		
		if(notExists)
		{
			box.addItem(item);
		}
	}
	
	private int installBundle(Collection<String> data)
	{
		int count = 0;
		if(data == null || data.size() == 0)
		{
			return count;
		}
		
		ClientProxyFactoryBean factory = getClientProxy();
		BundleServer server = (BundleServer) factory.create();
		
		count = server.install(data.toArray(new String[]{}));
		
		return count;
	}
	
	protected int controlOsgiBundle(JTable table, int state)
	{
		int count = 0;
		if(table == null)
		{
			return count;
		}
		
		int[] rows = table.getSelectedRows();
		
		if(rows.length == 0)
		{
			return count;
		}
		
		long[] ids = new long[rows.length];
		for(int i = 0; i < rows.length; i++)
		{
			int row = rows[i];
			Object value = table.getValueAt(row, 0);
			
			try
			{
				ids[i] = Long.parseLong(value.toString());
			}
			catch(NumberFormatException e)
			{
				ids[i] = -1;
			}
		}
		
		ClientProxyFactoryBean factory = getClientProxy();
		BundleServer server = (BundleServer) factory.create();
		
		switch(state)
		{
			case Start:
				count = server.start(ids);
				break;
			case Stop:
				count = server.stop(ids);
				break;
			case Update:
				count = server.update(ids);
				break;
			case Uninstall:
				count = server.uninstall(ids);
				break;
			default:
				System.out.println("unknow state control : " + state);
				break;
		}
		
		return count;
	}

	private JTable createCenter(JTable table)
	{
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		setTableHeader(table, HEAD_ID, HEAD_NAME, HEAD_VERSION, HEAD_STATE);
		
		final JSplitPane centerPanel = new JSplitPane();
		centerPanel.setLeftComponent(new JScrollPane(table));
		centerPanel.setRightComponent(createDetailInfoTable(table));
		centerPanel.setOneTouchExpandable(true);
		
		int location = (int) ((MainFrame.getInstance().getContentPanel().getWidth() - centerPanel.getDividerSize()) * 0.8);
		centerPanel.setDividerLocation(location);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				JButton divBut = getDividerBut(centerPanel, 1);
				if(divBut != null)
				{
					divBut.doClick();
				}
			}
		});
		
		panel.add(centerPanel, BorderLayout.CENTER);
		
		return table;
	}
	
	private JButton getDividerBut(JSplitPane splitPane, int index)
	{
		JButton splitBut = null;
		
		if(splitPane == null)
		{
			return splitBut;
		}
		
		SplitPaneUI ui = splitPane.getUI();
		if(ui instanceof BasicSplitPaneUI)
		{
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) ui).getDivider();
			
			try
			{
				Component divBut = divider.getComponent(index);
				if(divBut instanceof JButton)
				{
					splitBut = (JButton) divBut;
				}
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}
		}
		
		return splitBut;
	}

	private JPanel createDetailInfoTable(JTable table)
	{
		final JPanel panel = new JPanel();
		DefaultTableModel model = new DefaultTableModel();
		final JTable detailInfoTable = new JTable(model);
		final JScrollPane detailInfoScroll = new JScrollPane(detailInfoTable);
		
		setTableHeader(detailInfoTable, HEAD_NAME, HEAD_VALUE);
		
		TableCellTextAreaRenderer renderer = new TableCellTextAreaRenderer();
		renderer.setLineWrap(true);
		detailInfoTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		detailInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		table.addMouseListener(new MouseAdapter()
		{
			private int rowId;
			private JButton lastBut;
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int count = e.getClickCount();
				Object source = e.getSource();
				
				if(count == 2 && source instanceof JTable)
				{
					JTable table = (JTable) source;
					int id = table.getSelectedRow();
					if(id == rowId)
					{
						rowId = -1;
						hideDetail();
					}
					else
					{
						rowId = id;
						showDetial();
					}
					
					Object idValue = table.getValueAt(id, 0);
					
					getDetailInfo(idValue);
				}
			}
			
			private void showDetial()
			{
				detailControl(0);
			}
			
			private void hideDetail()
			{
				detailControl(1);
			}
			
			private void detailControl(int code)
			{
				Container parent = panel.getParent();
				if(parent instanceof JSplitPane)
				{
					JButton divBut = getDividerBut((JSplitPane) parent, code);
					
					if(divBut != lastBut && divBut != null)
					{
						lastBut = divBut;
						divBut.doClick();
					}
				}
			}
			
			private boolean getDetailInfo(Object idValue)
			{
				long id = -1;
				if(idValue == null)
				{
					return false;
				}
				
				try
				{
					id = Long.parseLong(idValue.toString());
				}
				catch(NumberFormatException e)
				{
				}
				
				if(id == -1)
				{
					return false;
				}
				
				ClientProxyFactoryBean factory = getClientProxy();
				BundleServer server = (BundleServer) factory.create();
				
				SuRenBundle bundle = server.getById(id);
				Vector<Object>[] data = new Vector[5];
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				
				data[0] = convertToVector("Id:", bundle.getId());
				data[1] = convertToVector("LastModified:", format.format(bundle.getLastModified()));
				data[2] = convertToVector("State:", bundle.getState());
				data[3] = convertToVector("Location:", bundle.getLocation());
				
				String headersStr = Arrays.toString(bundle.getHeaders());
				headersStr = headersStr.replace("], [", "\n");
				headersStr = headersStr.replace("[[", "");
				headersStr = headersStr.replace("]]", "");
				data[4] = convertToVector("Headers:", headersStr);
				
				fillTable(detailInfoTable, true, data);
				
				return true;
			}

			private Vector<Object> convertToVector(Object key, Object value)
			{
				Vector<Object> data = new Vector<Object>();
				
				data.add(key);
				data.add(value);
				
				return data;
			}
		});
		
		panel.setLayout(new BorderLayout());
		panel.add(detailInfoScroll, BorderLayout.CENTER);
		
		return panel;
	}

	private void loadOsgiInfo(JTable osgiTable)
	{
		loadOsgiInfo(osgiTable, null);
	}
	
	private void loadOsgiInfo(JTable osgiTable, String filterStr)
	{
		loadOsgiInfo(osgiTable, filterStr, true);
	}
	
	private void loadOsgiInfo(JTable osgiTable, String filterStr, boolean reload)
	{
		if(osgiTable == null)
		{
			return;
		}
		
		if(reload)
		{
			clearTable(osgiTable);
		}
		
		ClientProxyFactoryBean factory = getClientProxy();
		BundleServer server = (BundleServer) factory.create();
		
		List<SuRenBundle> bundles;
		if(filterStr != null && !("".equals(filterStr)))
		{
			bundles = server.searchBy(filterStr);
		}
		else
		{
			bundles = server.getAll();
		}
		
		for(SuRenBundle bundle : bundles)
		{
			Vector<Object> item = new Vector<Object>();
			
			item.add(bundle.getId());
			item.add(bundle.getName());
			item.add(bundle.getVersion());
			item.add(getDisplay(bundle.getState()));
			
			fillTable(osgiTable, item);
		}
		
		MainFrame main = MainFrame.getInstance();
		main.setTitle(bundles.size() + "==");
	}
	
	private void clearTable(JTable table)
	{
		if(table == null)
		{
			return;
		}
		
		TableModel tbModel = table.getModel();
		if(tbModel instanceof DefaultTableModel)
		{
			DefaultTableModel model = (DefaultTableModel) tbModel;
			
			int count = model.getRowCount();
			for(int i = 0; i < count; i++)
			{
				model.removeRow(0);
			}
		}
	}
	
	private ClientProxyFactoryBean getClientProxy()
	{
		String url = setting.getUrl() + ":" + setting.getPort() + "/greeter";
		
		if(!url.startsWith("http://") && !url.startsWith("https://"))
		{
			url = "http://" + url;
		}
		
		return getClientProxy(url, BundleServer.class);
	}
	
	private ClientProxyFactoryBean getClientProxy(String url, Class<BundleServer> clazz)
	{
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		
		factory.setAddress(url);
		factory.setServiceClass(BundleServer.class);
		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		
		return factory;
	}

	private void fillTable(JTable table, Vector<Object> ... datas)
	{
		fillTable(table, false, datas);
	}
	
	private void fillTable(JTable table, boolean reload, Vector<Object> ... datas)
	{
		if(table == null || datas == null)
		{
			return;
		}
		
		if(reload)
		{
			clearTable(table);
		}
		
		TableModel tbModel = table.getModel();
		if(tbModel instanceof DefaultTableModel)
		{
			DefaultTableModel model = (DefaultTableModel) tbModel;
			
			for(Vector<Object> data : datas)
			{
				model.addRow(data);
			}
		}
	}

	private void setTableHeader(JTable osgiTable, String ... headers)
	{
		if(osgiTable == null || headers == null)
		{
			return;
		}
		
		DefaultTableModel model = new DefaultTableModel(headers, 0){

			private static final long	serialVersionUID	= 4652694928615773932L;

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		
		osgiTable.setModel(model);
	}
	
	private String getDisplay(int code)
	{
		String display = "";
		
		switch(code)
		{
			case SuRenBundle.UNINSTALLED:
				display = "uninstalled";
				break;
			case SuRenBundle.INSTALLED:
				display = "installed";
				break;
			case SuRenBundle.RESOLVED:
				display = "resolved";
				break;
			case SuRenBundle.STARTING:
				display = "starting";
				break;
			case SuRenBundle.STOPPING:
				display = "stopping";
				break;
			case SuRenBundle.ACTIVE:
				display = "active";
				break;
		}
		
		return display;
	}
	
	class Setting
	{
		private String url;
		private int port;

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public int getPort()
		{
			return port;
		}

		public void setPort(int port)
		{
			this.port = port;
		}
	}
}