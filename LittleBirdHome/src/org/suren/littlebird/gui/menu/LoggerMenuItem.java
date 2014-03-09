package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
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
	private final String	HEAD_VALUE		= "value";
	
	private final int LogServerStarted = 0x1;
	private final int LogServerStoped = 0x2;
	private final int LogServerBind = 0x3;
	
	protected JPanel	panel;
	
	private LogServer server;
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
		final JToolBar settingBar = createSettingBar();
		controlBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		controlBar.setName("ControlBar");

		final JComboBox levelBox = new JComboBox();
		JButton setLevelBut = new JButton("SetLevel");
		final JComboBox filterBox = new JComboBox();
		final JButton reloadBut = new JButton("Reload");
		JButton settingBut = new JButton("Setting");
		SuRenStatusButton logSerControlBut = new SuRenStatusButton();
		
		logSerControlBut.addStatus(LogServerStarted, "Stop");
		logSerControlBut.addStatus(LogServerStoped, "Start");
		logSerControlBut.setStatus(LogServerStoped);
		
		controlBar.add(levelBox);
		controlBar.add(setLevelBut);
		controlBar.addSeparator();
		controlBar.add(filterBox);
		controlBar.add(reloadBut);
		controlBar.add(settingBut);
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
		settingBut.setMnemonic('g');
		logSerControlBut.setMnemonic('t');
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
					LoggerServer loggerServer = getLoggerServer();
					
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
		
		settingBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				settingBar.setVisible(!settingBar.isVisible());
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
				if(serverBind == null)
				{
					server = new LogServer();
					
					server.setLogLayout(getLogLayout());
					
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
						int port = getPort();
						if(port == -1)
						{
							System.err.println("invliad bridge port.");
							
							return;
						}
						
						server.setListener(logServerListener);
						boolean result = server.init(port);
						
						if(!result)
						{
							System.err.println("log server init failure.");
							
							return;
						}
						
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
			
			private String getLogLayout()
			{
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					return "";
				}
				
				return loggerMgrCfg.getLogLayout();
			}

			private int getPort()
			{
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					return -1;
				}
				
				return loggerMgrCfg.getBridgePort();
			}
		});
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		controlPanel.add(controlBar, BorderLayout.NORTH);
		controlPanel.add(settingBar, BorderLayout.CENTER);
		
		panel.add(controlPanel, BorderLayout.NORTH);
	}

	private JToolBar createSettingBar()
	{
		final JToolBar toolBar = new JToolBar();
		toolBar.setName("SettingBar");
		toolBar.setVisible(false);
		toolBar.setLayout(new GridLayout(3, 4));
		
		final JTextField remoteField = new JTextField();
		final JTextField remotePortField = new JTextField();
		final JTextField localPortField = new JTextField();
		final JTextField consoleBufferField = new JTextField();
		final JTextField logLayoutField = new JTextField();
		JButton cancelBut = new JButton("Cancel");
		JButton saveBut = new JButton("Save");
		
		cancelBut.setMnemonic('c');
		saveBut.setMnemonic('v');
		
		toolBar.add(new JLabel("Remote:"));
		toolBar.add(remoteField);
		toolBar.add(new JLabel("RemotePort:"));
		toolBar.add(remotePortField);
		toolBar.add(new JLabel("LocalPort:"));
		toolBar.add(localPortField);
		toolBar.add(new JLabel("LogLayout:"));
		toolBar.add(logLayoutField);
		toolBar.add(new JLabel("ConsoleBuffer:"));
		toolBar.add(consoleBufferField);
		toolBar.add(cancelBut);
		toolBar.add(saveBut);
		
		LoggerMgrSetting loggerMgrCfg = loadCfg();
		if(loggerMgrCfg != null)
		{
			remotePortField.setText(String.valueOf(loggerMgrCfg.getPort()));
			remoteField.setText(loggerMgrCfg.getHost());
			localPortField.setText(String.valueOf(loggerMgrCfg.getBridgePort()));
			consoleBufferField.setText(String.valueOf(loggerMgrCfg.getConsoleBuffer()));
			logLayoutField.setText(loggerMgrCfg.getLogLayout());
		}
		
		cancelBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toolBar.setVisible(false);
			}
		});
		
		saveBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int remotePort = -1;
				int localPort = - 1;
				String remotePortStr = remotePortField.getText();
				String remoteStr = remoteField.getText();
				String localPortStr = localPortField.getText();
				String logLayout = logLayoutField.getText();
				
				if("".equals(remotePortStr) || "".equals(remoteStr)
						|| "".equals(localPortStr)
						|| "".equals(logLayout))
				{
					return;
				}
				
				try
				{
					remotePort = Integer.parseInt(remotePortStr);
					localPort = Integer.parseInt(localPortStr);
				}
				catch(NumberFormatException e1)
				{
				}
				
				if(remotePort <= 0 || remotePort > 65536
						|| localPort <= 0 || localPort > 65536)
				{
					return;
				}
				
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					loggerMgrCfg = new LoggerMgrSetting();
				}
				
				loggerMgrCfg.setHost(remoteStr);
				loggerMgrCfg.setPort(remotePort);
				loggerMgrCfg.setBridgePort(localPort);
				loggerMgrCfg.setLogLayout(logLayout);
				
				if(saveCfg(loggerMgrCfg))
				{
					toolBar.setVisible(false);
				}
			}
		});
		
		return toolBar;
	}
	
	private void createCenter(SuRenTable table)
	{
		final SuRenTable detailTable = new SuRenTable();
		
		table.setHeaders(HEAD_NUM, HEAD_NAME, HEAD_LEVEL);
		table.setColumnSorterClass(0, Number.class);
		
		detailTable.setHeaders(HEAD_NAME, HEAD_VALUE);
		
		JTextArea logArea = new JTextArea();
		final JTextAreaAppender appender = new JTextAreaAppender();
		appender.setTargetArea(logArea);
		
		createTextPopuMenu(logArea);
		
		LoggerMgrSetting loggerMgrCfg = loadCfg();
		if(loggerMgrCfg != null)
		{
			int backColor = loggerMgrCfg.getBackColor();
			int foreColor = loggerMgrCfg.getForeColor();
			
			logArea.setBackground(new Color(backColor));
			logArea.setForeground(new Color(foreColor));
			logArea.setLineWrap(loggerMgrCfg.isLineWrap());
			appender.setRowsLimit(loggerMgrCfg.getConsoleBuffer());
		}
		
		JSplitPane loggerListSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(table),
				new JScrollPane(detailTable));
		setLocation(loggerListSplit, 0.8);
		
		JSplitPane centerSplit = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				loggerListSplit,
				new JScrollPane(logArea));
		setLocation(centerSplit, 0.6);
		
		table.addMouseListener(new MouseAdapter()
		{
			private int rowId;
			private JButton lastBut;
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int count = e.getClickCount();
				Object source = e.getSource();
				
				if(count == 2 && source instanceof SuRenTable)
				{
					SuRenTable table = (SuRenTable) source;
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
					
					Object nameValue = table.getValueAt(id, HEAD_NAME);
					getDetailInfo(nameValue);
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
			
			private boolean getDetailInfo(Object nameValue)
			{
				if(nameValue == null)
				{
					return false;
				}
				
				LoggerServer loggerServer = getLoggerServer();
				List<String> bridges = loggerServer.getBridges(nameValue.toString());
				if(bridges == null)
				{
					return false;
				}
				
				Vector<Object>[] data = new Vector[bridges.size()];
				int index = 0;
				for(String bridge : bridges)
				{
					convertToVector(index++, bridge);
				}
				fillTable(detailTable, true, data);
				
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
				appender.append(msg);
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
				
				String host = getLocalAddress();
				if(host == null)
				{
					System.err.println("invalid local address.");
					
					return;
				}
				
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				int port = -1;
				if(loggerMgrCfg == null || (port = loggerMgrCfg.getBridgePort()) <= 0
						|| port > 65536)
				{
					System.err.println("invalid config file.");
					
					return;
				}
				
				Object name = table.getValueAt(row, HEAD_NAME);
				LoggerServer loggerServer = getLoggerServer();
				
				boolean result = loggerServer.addBridge(name.toString(), host, port);
				System.out.println("add bridge result : " + result);
			}
		});
		
		delBridgeBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				if(row < 0)
				{
					return;
				}
				
				String host = getLocalAddress();
				if(host == null)
				{
					System.err.println("invalid local address.");
					
					return;
				}
				
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				int port = -1;
				if(loggerMgrCfg == null || (port = loggerMgrCfg.getBridgePort()) <= 0
						|| port > 65536)
				{
					System.err.println("invalid config file.");
					
					return;
				}
				
				Object name = table.getValueAt(row, HEAD_NAME);
				LoggerServer loggerServer = getLoggerServer();
				
				boolean result = loggerServer.removeBridge(name.toString(), host, port);
				System.out.println("remove bridge result : " + result);
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
		JMenuItem clearItem = new JMenuItem("Clear");
		
		menu.add(lineWrapItem);
		menu.add(backColorItem);
		menu.add(foreColorItem);
		menu.add(clearItem);
		
		lineWrapItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				logArea.setLineWrap(!logArea.getLineWrap());
				
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					loggerMgrCfg = new LoggerMgrSetting();
				}
				
				loggerMgrCfg.setLineWrap(logArea.getLineWrap());
				saveCfg(loggerMgrCfg);
			}
		});
		
		backColorItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					loggerMgrCfg = new LoggerMgrSetting();
				}
				
				Color color = JColorChooser.showDialog(logArea,
						"PickColor", new Color(loggerMgrCfg.getBackColor()));
				if(color == null)
				{
					return;
				}
				
				logArea.setBackground(color);
				
				loggerMgrCfg.setBackColor(color.getRGB());
				saveCfg(loggerMgrCfg);
			}
		});
		
		foreColorItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				LoggerMgrSetting loggerMgrCfg = loadCfg();
				if(loggerMgrCfg == null)
				{
					loggerMgrCfg = new LoggerMgrSetting();
				}
				
				Color color = JColorChooser.showDialog(logArea,
						"PickColor", new Color(loggerMgrCfg.getForeColor()));
				if(color == null)
				{
					return;
				}
				
				logArea.setForeground(color);
				
				loggerMgrCfg.setForeColor(color.getRGB());
				saveCfg(loggerMgrCfg);
			}
		});
		
		clearItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				logArea.setText("");
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
		LoggerServer loggerServer = getLoggerServer();
		
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
	
	private LoggerServer getLoggerServer()
	{
		LoggerMgrSetting loggerMgrCfg = loadCfg();
		if(loggerMgrCfg == null)
		{
			return null;
		}
		
		String host = loggerMgrCfg.getHost();
		int port = loggerMgrCfg.getPort();
		
		updateStatus(host, ":", port);
		
		ClientProxy<LoggerServer> proxy =
				new ClientProxy<LoggerServer>(host, port, "logger");
		ClientProxyFactoryBean factory = proxy.getClientProxy(LoggerServer.class);
		
		return (LoggerServer) factory.create();
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
	
	private String getLocalAddress()
	{
		LoggerMgrSetting loggerMgrCfg = loadCfg();
		if(loggerMgrCfg == null)
		{
			return null;
		}
		
		String host = loggerMgrCfg.getHost();
		int port = loggerMgrCfg.getPort();
		
		Socket socket = null;
		try
		{
			InetAddress addr = InetAddress.getByName(host);
			socket = new Socket(addr, port);
			socket.setSoTimeout(1000);
			
			return socket.getLocalAddress().getHostAddress();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(socket != null)
			{
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
}
