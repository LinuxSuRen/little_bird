package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
import org.suren.littlebird.gui.SuRenComboBox;
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
	private final String 	HEAD_BRIDGES	= "bridges";
	private final String	HEAD_VALUE		= "value";
	
	private final int LogServerStarted = 0x1;
	private final int LogServerStoped = 0x2;
	private final int LogServerBind = 0x3;
	
	protected JPanel	panel;
	
	private LogServer server;
	private LogServerListener logServerListener;
	private JTextAreaAppender appender;
	
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
		final SuRenComboBox filterBox = new SuRenComboBox();
		final JButton reloadBut = new JButton("Reload");
		JButton settingBut = new JButton("Setting");
		final JTextField bridgeField = new JTextField(10);
		JButton cleanBridgeBut = new JButton("CleanBridge");
		final JTextField filterField = new JTextField(10);
		JButton addFilterBut = new JButton("AddFilter");
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
		controlBar.add(bridgeField);
		controlBar.add(cleanBridgeBut);
		controlBar.add(filterField);
		controlBar.add(addFilterBut);
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
		cleanBridgeBut.setMnemonic('c');
		bridgeField.setToolTipText("Bridges");
		filterBox.setEditable(true);
		filterBox.setToolTipText("Ctrl+E");
		filterBox.registerKeyboardAction(new FocusAndSelectListener(),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		LoggerMgrSetting loggerMgrCfg = loadCfg();
		if(loggerMgrCfg != null)
		{
			Set<String> hisKeyword = loggerMgrCfg.getHistoryKeyword();
			if(hisKeyword != null)
			{
				for(String keyword : hisKeyword)
				{
					filterBox.addItem(keyword);
				}
			}
		}
		
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
		
		filterBox.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				String cmd = e.getActionCommand();
				
				if("comboBoxEdited".equals(cmd) && source instanceof SuRenComboBox)
				{
					reloadBut.doClick();
					
					SuRenComboBox filter = (SuRenComboBox) source;
					save(filter.getSelectedItem().toString());
					
					filter.addUniItem(filter.getSelectedItem());
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
					reload(table, item.toString());
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
		
		cleanBridgeBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				LoggerMgrSetting cfg = loadCfg();
				if(cfg == null)
				{
					return;
				}
				
				LoggerServer loggerServer = getLoggerServer();
				String bridgeName = bridgeField.getText();
				int[] rows = table.getSelectedRows();
				boolean result = false;
				
				if("".equals(bridgeName))
				{
					for(int row : rows)
					{
						Object name = table.getValueAt(row, HEAD_NAME);
						if(name == null)
						{
							continue;
						}
						
						result = result | loggerServer.removeBridge(name.toString(),
								getLocalAddress(),
								cfg.getBridgePort());
					}
				}
				else
				{
					int count = loggerServer.clearBridges(bridgeName);
					
					appender.appendLine("Clear Bridges : " + count);
					
					result = count > 0;
				}
				
				if(result)
				{
					reloadBut.doClick();
				}
			}
		});
		
		addFilterBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String filter = filterField.getText();
				if("".equals(filter))
				{
					return;
				}

				int[] rows = table.getSelectedRows();
				LoggerServer loggerServer = getLoggerServer();
				
				for(int row : rows)
				{
					Object name = table.getValueAt(row, HEAD_NAME);
					if(name == null)
					{
						continue;
					}

					loggerServer.clearFilter(name.toString(), "127.0.0.16789");
					loggerServer.addFilter(name.toString(), "127.0.0.16789", filter);
				}
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
		toolBar.setLayout(new GridLayout(4, 4));
		
		final JTextField remoteField = new JTextField();
		final JTextField remotePortField = new JTextField();
		final JTextField localPortField = new JTextField();
		final JTextField consoleBufferField = new JTextField();
		final JTextField logLayoutField = new JTextField();
		final JComboBox agentTypeBox = new JComboBox();
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
		toolBar.add(new JLabel("AgentType:"));
		toolBar.add(agentTypeBox);
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
			
			Set<String> agentType = loggerMgrCfg.getAgentType();
			if(agentType != null)
			{
				for(Object agent : agentType.toArray())
				{
					agentTypeBox.addItem(agent);
				}
			}
			agentTypeBox.setSelectedItem(loggerMgrCfg.getAgent());
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
				int consoleBuffer = -1;
				String remotePortStr = remotePortField.getText();
				String remoteStr = remoteField.getText();
				String localPortStr = localPortField.getText();
				String logLayout = logLayoutField.getText();
				String consoleBufferText = consoleBufferField.getText();
				String agent = agentTypeBox.getSelectedItem().toString();
				
				if("".equals(remotePortStr) || "".equals(remoteStr)
						|| "".equals(localPortStr)
						|| "".equals(logLayout)
						|| "".equals(consoleBufferText))
				{
					return;
				}
				
				try
				{
					remotePort = Integer.parseInt(remotePortStr);
					localPort = Integer.parseInt(localPortStr);
					consoleBuffer = Integer.parseInt(consoleBufferText);
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
				loggerMgrCfg.setConsoleBuffer(consoleBuffer);
				loggerMgrCfg.setAgent(agent);
				
				if(saveCfg(loggerMgrCfg))
				{
					toolBar.setVisible(false);
					
					if(server != null)
					{
						server.setLogLayout(logLayout);
					}
				}
			}
		});
		
		return toolBar;
	}
	
	private void createCenter(SuRenTable table)
	{
		final SuRenTable detailTable = new SuRenTable();
		
		table.setHeaders(HEAD_NUM, HEAD_NAME, HEAD_LEVEL, HEAD_BRIDGES);
		table.setColumnSorterClass(0, Number.class);
		table.setColumnSorterClass(3, Number.class);
		
		detailTable.setHeaders(HEAD_NUM, HEAD_VALUE);
		
		JTextArea logArea = new JTextArea();
		appender = new JTextAreaAppender();
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
		
		JPanel detailPanel = createDetailPanel(detailTable);
		
		JSplitPane loggerListSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(table),
				new JScrollPane(detailPanel));
		loggerListSplit.setOneTouchExpandable(true);
		setLocation(loggerListSplit, 0.8);
		
		JSplitPane centerSplit = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				loggerListSplit,
				new JScrollPane(logArea));
		centerSplit.setOneTouchExpandable(true);
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
					
					detailTable.addData(HEAD_NAME, nameValue);
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
					data[index] = convertToVector(String.valueOf(index), bridge);
					
					index++;
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

	private JPanel createDetailPanel(final SuRenTable detailTable)
	{
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(layout);
		
		final JPanel infoPanel = new JPanel();
		final GridBagLayout infoLayout = new GridBagLayout();
		infoPanel.setLayout(infoLayout);
		infoPanel.setBackground(Color.PINK);
		
		GridBagConstraints cons = new GridBagConstraints();
		
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx  = 1;
		cons.weighty  = 0;
		cons.gridx = 1;
		cons.gridy = 1;
		cons.anchor = GridBagConstraints.WEST;
		layout.setConstraints(detailTable, cons);
		panel.add(detailTable);
		
		cons.gridy = 2;
		cons.weighty  = 0;
		layout.setConstraints(infoPanel, cons);
		panel.add(infoPanel);
		
		JPanel leftPanel = new JPanel();
		cons.gridy = 3;
		cons.weighty  = 1;
		layout.setConstraints(leftPanel, cons);
		panel.add(leftPanel);
		
		detailTable.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{
				int count = e.getClickCount();
				if(count < 2)
				{
					return;
				}
				
				int rowId = detailTable.getSelectedRow();
				Object loggerName = detailTable.getData().get(HEAD_NAME);
				Object name;
				
				if(rowId == -1 || loggerName == null
						|| (name = detailTable.getValueAt(rowId, HEAD_VALUE)) == null)
				{
					return;
				}
				
				LoggerServer loggerServer = getLoggerServer();
				if(loggerServer == null)
				{
					return;
				}
				
				List<Entry<String, String>> bridgeInfo = loggerServer.bridgeInfo(
						loggerName.toString(), name.toString());
				if(bridgeInfo != null)
				{
					infoPanel.removeAll();
					gridLayout.setRows(bridgeInfo.size());
					
					GridBagConstraints cons = new GridBagConstraints();
					int len = bridgeInfo.size();
					
					for(int i = 0; i < len;)
					{
						Entry<String, String> entry = bridgeInfo.get(i);
						i++;
						
						JLabel keyLabel = new JLabel(entry.getKey() + " : ");
						cons.gridx = 1;
						cons.gridy = i;
						cons.weightx = 0;
						cons.anchor = GridBagConstraints.WEST;
						cons.ipadx = 15;
						infoLayout.setConstraints(keyLabel, cons);
						infoPanel.add(keyLabel);

						JLabel valueLabel = new JLabel(entry.getValue());
						cons.gridx = 2;
						cons.weightx = 1;
						infoLayout.setConstraints(valueLabel, cons);
						infoPanel.add(valueLabel);
					}
				}
			}
		});
		
		return panel;
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
		JMenuItem autoScollItem = new JMenuItem("AutoScoll");
		JMenuItem backColorItem = new JMenuItem("BackColor");
		JMenuItem foreColorItem = new JMenuItem("ForeColor");
		JMenuItem clearItem = new JMenuItem("Clear");
		
		menu.add(lineWrapItem);
		menu.add(autoScollItem);
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
		
		autoScollItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				appender.setAutoScoll(!appender.isAutoScoll());
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
	
	@SuppressWarnings("unchecked")
	private int reload(SuRenTable table, String filter, boolean reload)
	{
		LoggerServer loggerServer = getLoggerServer();
		
		List<List<Entry<String, String>>> loggers = null;
		if(filter == null)
		{
			loggers = loggerServer.getAllLoggers();
		}
		else
		{
			loggers = loggerServer.searchLoggersBy(filter);
		}
		
		if(loggers == null)
		{
			return 0;
		}
		
//		ByteArrayInputStream byteInput = new ByteArrayInputStream(loggersByte);
//		ObjectInputStream objectInput;
//		try
//		{
//			objectInput = new ObjectInputStream(byteInput);
//			Object sourceObj = objectInput.readObject();
//			
//			if(sourceObj instanceof List<?>)
//			{
//				loggers = (List<List<Entry<String, String>>>) sourceObj;
//			}
//			else
//			{
//				return 0;
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		catch (ClassNotFoundException e)
//		{
//			e.printStackTrace();
//		}
		
		int len = loggers.size();
		Vector<Object>[] items = new Vector[len];
		for(int i = 0; i < len; ++i)
		{
			Vector<Object> item = new Vector<Object>();
			
			String name = "";
			String level = "";
			int count = 0;
			
			List<Entry<String, String>> logger = loggers.get(i);
			for(Entry<String, String> entry : logger)
			{
				if("name".equals(entry.getKey()))
				{
					name = entry.getValue();
				}
				else if("level".equals(entry.getKey()))
				{
					level = entry.getValue();
				}
				else if("bridges_count".equals(entry.getKey()))
				{
					try
					{
						count = Integer.parseInt(entry.getValue());
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			item.add(i);
			item.add(name);
			item.add(level);
			item.add(count);
			
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
		String agent = loggerMgrCfg.getAgent();
		
		updateStatus(host, ":", port);
		
		if("webservice".equalsIgnoreCase(agent))
		{
			ClientProxy<LoggerServer> proxy =
					new ClientProxy<LoggerServer>(host, port, "logger");
			ClientProxyFactoryBean factory = proxy.getClientProxy(LoggerServer.class);
			
			return (LoggerServer) factory.create();
		}
		else if("jmx".equalsIgnoreCase(agent))
		{
			try
			{
				String url = "rmi://" + host + ":" + port + "/RemoteLoggerServer";
				LoggerServer logger = (LoggerServer) Naming.lookup(url);
				
				return logger;
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
			catch (NotBoundException e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
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
