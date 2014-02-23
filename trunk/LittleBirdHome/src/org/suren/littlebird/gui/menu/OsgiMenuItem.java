package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.server.SuRenBundle;

@Menu(displayName = "Osgi", parentMenu = RemoteMenu.class, index = 1)
public class OsgiMenuItem extends ArchMenu
{
	private final String	HEAD_ID			= "id";
	private final String	HEAD_NAME		= "name";
	private final String	HEAD_VERSION	= "version";
	private final String	HEAD_STATE		= "state";
	
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
		
		JTable table = createCenter();
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
		
		filterBox.setEditable(true);
		
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
				settingBar.setVisible(true);
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

				setting.setUrl(urlField.getText());
				setting.setPort(port);
				
				settingBar.setVisible(false);
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

	private JTable createCenter()
	{
		JTable osgiTable = new JTable();
		osgiTable.setAutoCreateRowSorter(true);
		setTableHeader(osgiTable, HEAD_ID, HEAD_NAME, HEAD_VERSION, HEAD_STATE);
		
		JScrollPane osgiPane = new JScrollPane(osgiTable);
		
		panel.add(osgiPane, BorderLayout.CENTER);
		
		return osgiTable;
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
			TableModel tbModel = osgiTable.getModel();
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
		if(table == null || datas == null)
		{
			return;
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