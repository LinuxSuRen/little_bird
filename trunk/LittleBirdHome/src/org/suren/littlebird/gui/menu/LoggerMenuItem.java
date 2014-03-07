package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.log4j.Level;
import org.suren.littlebird.ClientInfo;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.FocusAndSelectListener;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenStatusButton;
import org.suren.littlebird.gui.SuRenTable;
import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.gui.log.JTextAreaAppender;
import org.suren.littlebird.net.webservice.ClientProxy;
import org.suren.littlebird.server.LogServer;
import org.suren.littlebird.server.LogServerListener;
import org.suren.littlebird.server.LoggerServer;
import org.suren.littlebird.setting.LoggerMgrSetting;
import org.suren.littlebird.setting.SettingUtil;

@Menu(displayName = "Logger", parentMenu = RemoteMenu.class, index = 2,
		keyCode = KeyEvent.VK_L, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class LoggerMenuItem extends ArchMenu<LoggerMgrSetting>
{
	private final String	LOGGER_CFG_PATH	= "logger_mgr_cfg.xml";
	private final String	HEAD_NUM		= "num";
	private final String	HEAD_NAME		= "name";
	private final String	HEAD_LEVEL		= "level";
	
	private final int LogServerStarted = 0x1;
	private final int LogServerStoped = 0x2;
	private final int LogServerBind = 0x3;
	
	protected JPanel	panel;
	
	private LogServerListener logServerListener;
	
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
				main.getContentPanel().add("Logger", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Logger");
			
			main.reDrawPanel();
		}
	};

	protected void init()
	{
		setStatusLabel(new JLabel());
		panel.add(getStatusLabel(), BorderLayout.SOUTH);
		
		SuRenTable table = new SuRenTable();
		createToolBar(table);
		createCenter(table);
		createCenterPopuMenu(table);
	}

	private void createToolBar(final SuRenTable table)
	{
		JToolBar controlBar = new JToolBar();
		controlBar.setLayout(new FlowLayout(FlowLayout.LEFT));

		final JComboBox levelBox = new JComboBox();
		JButton setLevelBut = new JButton("SetLevel");
		final JComboBox filterBox = new JComboBox();
		final JButton reloadBut = new JButton("Reload");
		SuRenStatusButton logSerControlBut = new SuRenStatusButton();
		
		logSerControlBut.addStatus(LogServerStarted, "Stop");
		logSerControlBut.addStatus(LogServerStoped, "Start");
		logSerControlBut.setStatus(LogServerStoped);
		
		controlBar.add(levelBox);
		controlBar.add(setLevelBut);
		controlBar.addSeparator();
		controlBar.add(filterBox);
		controlBar.add(reloadBut);
		controlBar.addSeparator();
		controlBar.add(logSerControlBut);
		
		levelBox.addItem(Level.ALL.toString());
		levelBox.addItem(Level.TRACE.toString());
		levelBox.addItem(Level.DEBUG.toString());
		levelBox.addItem(Level.INFO.toString());
		levelBox.addItem(Level.WARN.toString());
		levelBox.addItem(Level.ERROR.toString());
		levelBox.addItem(Level.FATAL.toString());
		levelBox.addItem(Level.OFF.toString());
		
		setLevelBut.setMnemonic('s');
		reloadBut.setMnemonic('r');
		filterBox.setEditable(true);
		filterBox.setToolTipText("Ctrl+E");
		filterBox.registerKeyboardAction(new FocusAndSelectListener(),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		setLevelBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] rows = table.getSelectedRows();
				for(int row : rows)
				{
					Object value = table.getValueAt(row, HEAD_NAME);
					
					ClientProxy<LoggerServer> proxy =
							new ClientProxy<LoggerServer>("10.0.31.249", 9789, "logger");
					ClientProxyFactoryBean factory = proxy.getClientProxy(LoggerServer.class);
					
					LoggerServer loggerServer = (LoggerServer) factory.create();
					
					loggerServer.setLevel(value.toString(), levelBox.getSelectedItem().toString());
				}
				
				if(rows.length > 0)
				{
					reloadBut.doClick();
				}
			}
		});
		
		reloadBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object item = filterBox.getSelectedItem();
				
				if(item == null || "".equals(item.toString()))
				{
					reload(table);
				}
				else
				{
					int count = reload(table, item.toString());
					
					if(count > 0)
					{
						save(item.toString());
					}
				}
			}
			
			private void save(String item)
			{
				LoggerMgrSetting cfg = loadCfg();
				
				if(cfg == null)
				{
					cfg = new LoggerMgrSetting();
				}
				
				cfg.addHistoryKeyword(item);
				saveCfg(cfg);
			}
		});
		
		logSerControlBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				SuRenStatusButton button;
				if(!(source instanceof SuRenStatusButton))
				{
					return;
				}
				
				button = (SuRenStatusButton) source;
				Object serverBind = button.getTarget(LogServerBind);
				LogServer server;
				if(serverBind == null)
				{
					server = new LogServer();
					
					button.addTarget(LogServerBind, server);
				}
				else if(serverBind instanceof LogServer)
				{
					server = (LogServer) serverBind;
				}
				else
				{
					System.out.println("LogServer bind error.");
					
					return;
				}
				
				switch(button.getStatus())
				{
					case LogServerStoped:
						server.setListener(logServerListener);
						server.init(7890);
						
						try
						{
							new Thread(server, "LogServer").start();
							
							button.setStatus(LogServerStarted);
						}
						catch(IllegalThreadStateException e1)
						{
							e1.printStackTrace();
						}
						
						break;
					case LogServerStarted:
						if(server.stop())
						{
							button.setStatus(LogServerStoped);
						}
						
						break;
					default:
						System.err.println("unknow logServer status.");
						break;
				}
			}
		});
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		controlPanel.add(controlBar, BorderLayout.NORTH);
		
		panel.add(controlPanel, BorderLayout.NORTH);
	}
	
	private void createCenter(SuRenTable table)
	{
		table.setHeaders(HEAD_NUM, HEAD_NAME, HEAD_LEVEL);
		table.setColumnSorterClass(0, Number.class);
		
		JScrollPane scrollPane = new JScrollPane(table);

		JTextArea logArea = new JTextArea();
		JScrollPane logScroll = new JScrollPane(logArea);
		final JTextAreaAppender appender = new JTextAreaAppender();
		appender.setTargetArea(logArea);
		
		createTextPopuMenu(logArea);
		
		JSplitPane loggerListSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				scrollPane,
				new JScrollPane(new SuRenTable()));
		setLocation(loggerListSplit, 0.8);
		
		JSplitPane centerSplit = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				loggerListSplit,
				logScroll);
		setLocation(centerSplit, 0.8);
		
		logServerListener = new LogServerListener()
		{
			
			@Override
			public void onLine(ClientInfo clientInfo)
			{
			}
			
			@Override
			public void offLine(ClientInfo clientInfo)
			{
			}
			
			@Override
			public void printMessage(CharSequence msg)
			{
				appender.appendLine(msg);
			}
		};
		
		panel.add(centerSplit, BorderLayout.CENTER);
	}

	private void createCenterPopuMenu(final SuRenTable table)
	{
		if(table == null)
		{
			return;
		}
		
		final JPopupMenu menu = new JPopupMenu();
		
		JMenuItem addBridgeBut = new JMenuItem("AddBridge");
		JMenuItem delBridgeBut = new JMenuItem("DelBridge");
		
		menu.add(addBridgeBut);
		menu.add(delBridgeBut);
		
		table.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int code = e.getButton();
				if(code != MouseEvent.BUTTON3)
				{
					return;
				}
				
				int row = table.getSelectedRow();
				if(row < 0)
				{
					return;
				}
				
				menu.show(table, e.getX(), e.getY());
			}
		});
		
		addBridgeBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				if(row < 0)
				{
					return;
				}
				
				Object name = table.getValueAt(row, HEAD_NAME);
				ClientProxy<LoggerServer> proxy =
						new ClientProxy<LoggerServer>("10.0.31.249", 9789, "logger");
				ClientProxyFactoryBean factory = proxy.getClientProxy(LoggerServer.class);
				
				LoggerServer loggerServer = (LoggerServer) factory.create();
				
				boolean result = loggerServer.addBridge(name.toString(), "10.0.31.249", 7890);
				System.out.println("add bridge result : " + result);
			}
		});
	}

	private void createTextPopuMenu(final JTextArea logArea)
	{
		if(logArea == null)
		{
			return;
		}
		
		final JPopupMenu menu = new JPopupMenu();
		
		JMenuItem lineWrapItem = new JMenuItem("LineWrap");
		JMenuItem backColorItem = new JMenuItem("BackColor");
		JMenuItem foreColorItem = new JMenuItem("ForeColor");
		
		menu.add(lineWrapItem);
		menu.add(backColorItem);
		menu.add(foreColorItem);
		
		lineWrapItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				logArea.setLineWrap(!logArea.getLineWrap());
			}
		});
		
		backColorItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Color color = JColorChooser.showDialog(logArea,
						"PickColor", Color.PINK);
				
				logArea.setBackground(color);
			}
		});
		
		foreColorItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Color color = JColorChooser.showDialog(logArea,
						"PickColor", Color.PINK);
				
				logArea.setForeground(color);
			}
		});
		
		logArea.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int code = e.getButton();
				Object source = e.getSource();
				
				if(!(source instanceof JTextArea))
				{
					return;
				}
				
				if(code != MouseEvent.BUTTON3)
				{
					return;
				}
				
				menu.show((JTextArea) source, e.getX(), e.getY());
			}
		});
	}

	private void setLocation(JSplitPane splitPane, double location)
	{
		if(splitPane == null || location < 0 || location > 1)
		{
			return;
		}
		
		MainFrame mainFrame = MainFrame.getInstance();
		int parentSize = -1;
		int divWidth = splitPane.getDividerSize();
		
		switch(splitPane.getOrientation())
		{
			case JSplitPane.HORIZONTAL_SPLIT:
				parentSize = mainFrame.getContentPanel().getWidth();
				break;
			case JSplitPane.VERTICAL_SPLIT:
				parentSize = mainFrame.getContentPanel().getHeight();
				break;
			default:
				return;
		}
		
		splitPane.setDividerLocation((int)((parentSize - divWidth) * location));
	}

	private int reload(SuRenTable table)
	{
		return reload(table, null, true);
	}
	
	private int reload(SuRenTable table, String filter)
	{
		return reload(table, filter, true);
	}
	
	private int reload(SuRenTable table, String filter, boolean reload)
	{
		updateStatus("10.0.31.249");
		ClientProxy<LoggerServer> proxy =
				new ClientProxy<LoggerServer>("10.0.31.249", 9789, "logger");
		ClientProxyFactoryBean factory = proxy.getClientProxy(LoggerServer.class);
		
		LoggerServer loggerServer = (LoggerServer) factory.create();
		
		List<String> names;
		if(filter == null)
		{
			names = loggerServer.getNames();
		}
		else
		{
			names = loggerServer.searchBy(filter);
		}
		
		int len = names.size();
		Vector<Object>[] items = new Vector[len];
		for(int i = 0; i < len; ++i)
		{
			Vector<Object> item = new Vector<Object>();
			
			String name = names.get(i);
			
			item.add(i);
			item.add(name);
			item.add(loggerServer.getLevel(name));
			
			items[i] = item;
		}
		fillTable(table, items);
		
		return len;
	}

	private void fillTable(SuRenTable table, Vector<Object> ... datas)
	{
		fillTable(table, true, datas);
	}
	
	private void fillTable(SuRenTable table, boolean reload, Vector<Object> ... datas)
	{
		if(table == null || datas == null)
		{
			return;
		}
		
		if(reload)
		{
			clearTable(table);
		}
		
		SuRenTableModel model = table.getModel();
		for(Vector<Object> data : datas)
		{
			model.addRow(data);
		}
	}
	
	private void clearTable(SuRenTable table)
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

	@Override
	protected boolean saveCfg(LoggerMgrSetting cfgObj)
	{
		SettingUtil<LoggerMgrSetting> settingUtil = new SettingUtil<LoggerMgrSetting>();
		
		return settingUtil.save(cfgObj, LOGGER_CFG_PATH, LoggerMgrSetting.class);
	}
	
	private LoggerMgrSetting getLoggerCfg(String path)
	{
		SettingUtil<LoggerMgrSetting> settingUtil = new SettingUtil<LoggerMgrSetting>();
		
		LoggerMgrSetting data = settingUtil.load(path, LoggerMgrSetting.class);
		
		return data;
	}

	@Override
	protected LoggerMgrSetting loadCfg()
	{
		return getLoggerCfg(LOGGER_CFG_PATH);
	}

}
