package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
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
import java.io.IOException;
import java.io.InputStream;
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
import javax.swing.JTextArea;
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
import org.suren.littlebird.exception.SuRenSettingException;
import org.suren.littlebird.gui.FocusAndSelectListener;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenFilterBox;
import org.suren.littlebird.gui.SuRenTable;
import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.gui.log.JTextAreaAppender;
import org.suren.littlebird.io.IoUtil;
import org.suren.littlebird.net.ssh.JSchConnection;
import org.suren.littlebird.net.ssh.SimpleUserInfo;
import org.suren.littlebird.po.SuRenBundle;
import org.suren.littlebird.server.BundleServer;
import org.suren.littlebird.setting.OsgiMgrSetting;
import org.suren.littlebird.setting.SettingUtil;
import org.suren.littlebird.util.NumberParser;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Menu(displayName = "Osgi", parentMenu = RemoteMenu.class, index = 1,
	keyCode = KeyEvent.VK_I, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class OsgiMenuItem extends ArchMenu<OsgiMgrSetting>
{
	private final String	OSGI_CFG_PATH	= "osgi_mgr_cfg.xml";
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
		
		setStatusLabel(new JLabel());
		panel.add(getStatusLabel(), BorderLayout.SOUTH);
		
		setting = new Setting();
		
		SuRenTable table = new SuRenTable();
		createCenter(table);
		createToolBar(table);
	}
	
	private void createToolBar(final SuRenTable table)
	{
		final Set<String> data = new HashSet<String>();
		final JToolBar controlBar = new JToolBar();
		final JToolBar installBar = createInstallBar(data);
		final JToolBar cmdBar = createCmdBar(data);
		final JToolBar settingBar = createSettingBar();
		
		final JButton reloadBut = new JButton("Reload");
		JButton startBut = new JButton("Start");
		JButton stopBut = new JButton("Stop");
		JButton updateBut = new JButton("Update");
		JButton installBut = new JButton("Install");
		JButton uninstallBut = new JButton("Uninstall");
		JButton cmdBut = new JButton("Cmd");
		final SuRenFilterBox<Object> filterBox = new SuRenFilterBox<Object>();
		JButton settingBut = new JButton("Setting");
		
		reloadBut.setMnemonic('r');
		startBut.setMnemonic('s');
		stopBut.setMnemonic('t');
		updateBut.setMnemonic('e');
		installBut.setMnemonic('i');
		uninstallBut.setMnemonic('u');
		cmdBut.setMnemonic('m');
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
		controlBar.add(cmdBut);
		controlBar.add(filterBox);
		controlBar.add(settingBut);
		
		OsgiMgrSetting osgiCfg = loadCfg();
		if(osgiCfg != null)
		{
			Set<String> hisPath = osgiCfg.getHistoryPath();
			if(hisPath != null)
			{
				for(String path : hisPath)
				{
					filterBox.addItem(path);
				}
				
				filterBox.setSource(Arrays.asList(hisPath.toArray()));
			}
			
			filterBox.setSelectedItem(osgiCfg.getPath());
		}
		
		reloadBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object item = filterBox.getSelectedItem();

				int[] rows = table.getSelectedRows();
				
				if(item == null)
				{
					loadOsgiInfo(table);
				}
				else
				{
					loadOsgiInfo(table, item.toString());
				}
				
				table.rowsSelect(rows);
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
		
		cmdBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cmdBar.setVisible(!cmdBar.isVisible());
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
					String path = item.toString();
					
					loadOsgiInfo(table, path);
					
					if(table.getRowCount() > 0)
					{
						collectForOnly(filter, path);
						
						OsgiMgrSetting osgiCfg = loadCfg();
						osgiCfg.setPath(path);
						osgiCfg.addHistoryPath(path);
						
						saveCfg(osgiCfg);
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
//		controlPanel.setLayout(new BorderLayout());
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));		
		
		controlPanel.add(controlBar);//, BorderLayout.NORTH);
		controlPanel.add(installBar);//, BorderLayout.CENTER);
		controlPanel.add(cmdBar);//, BorderLayout.CENTER);
		controlPanel.add(settingBar);//, BorderLayout.SOUTH);
		
		panel.add(controlPanel, BorderLayout.NORTH);
	}

	private JToolBar createSettingBar()
	{
		final JToolBar settingBar = new JToolBar();
		GridLayout gridLayout = new GridLayout();
		gridLayout.setRows(4);
		settingBar.setVisible(false);
		settingBar.setLayout(gridLayout);

		final JComboBox urlField = new JComboBox();
		final JTextField portField = new JTextField();
		final JTextField sshUserField = new JTextField();
		final JTextField sshPwdField = new JTextField();
		final JTextField sshPortField = new JTextField();
		final JTextField outputBufField = new JTextField();
		JButton saveBut = new JButton("save");
		
		urlField.setEditable(true);
		saveBut.setMnemonic('v');
		
		settingBar.add(new JLabel("url"));
		settingBar.add(urlField);
		settingBar.add(new JLabel("port"));
		settingBar.add(portField);
		settingBar.add(new JLabel("SshUser"));
		settingBar.add(sshUserField);
		settingBar.add(new JLabel("SshPassword"));
		settingBar.add(sshPwdField);
		settingBar.add(new JLabel("SshPort"));
		settingBar.add(sshPortField);
		settingBar.add(new JLabel("OutputBuffer"));
		settingBar.add(outputBufField);
		settingBar.add(saveBut);
		
		OsgiMgrSetting data = loadCfg();
		
		if(data != null)
		{
			portField.setText(String.valueOf(data.getPort()));
			
			setting.setUrl(data.getHost());
			setting.setPort(data.getPort());
			
			Set<String> hisUrl = data.getHistoryUrl();
			if(hisUrl != null)
			{
				for(String url : hisUrl)
				{
					urlField.addItem(url);
				}
			}
			
			urlField.setSelectedItem(data.getHost());
			
			updateStatus(data.getHost());
			
			sshUserField.setText(data.getSshUser());
			sshPwdField.setText(data.getSshPwd());
			sshPortField.setText(String.valueOf(data.getSshPort()));
			outputBufField.setText(String.valueOf(data.getOutputBuffer()));
		}
		
		saveBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object item = urlField.getSelectedItem();
				if(item == null || "".equals(item.toString()))
				{
					return;
				}
				
				String url = item.toString();
				String strPort = portField.getText();
				int sshPort = NumberParser.parseTo(sshPortField, -1);
				int port = NumberParser.parseTo(strPort, -1);
				int outputBuf = NumberParser.parseTo(outputBufField, -1);
				
				if(url.equals("") || port <= 0 || port >= 65536
						|| outputBuf == -1)
				{
					return;
				}

				setting.setUrl(url);
				setting.setPort(port);
				setting.setSshUser(sshUserField.getText());
				setting.setSshPassword(sshPwdField.getText());
				setting.setSshPort(sshPort);
				setting.setOutputBuf(outputBuf);
				
				settingBar.setVisible(false);
				
				save(setting);
			}
			
			private boolean save(Setting setting)
			{
				if(setting == null)
				{
					return false;
				}
				
				OsgiMgrSetting osgiSetting = loadCfg();
				if(osgiSetting == null)
				{
					osgiSetting = new OsgiMgrSetting();
				}
				
				osgiSetting.setHost(setting.getUrl());
				osgiSetting.setPort(setting.getPort());
				osgiSetting.addHistoryUrl(setting.getUrl());
				osgiSetting.setSshUser(setting.getSshUser());
				osgiSetting.setSshPwd(setting.getSshPassword());
				osgiSetting.setSshPort(setting.getSshPort());
				osgiSetting.setOutputBuffer(setting.getOutputBuf());
				
				boolean result = saveCfg(osgiSetting);
				if(result)
				{
					updateStatus(setting.getUrl());
				}
				
				return result;
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
		SuRenFilterBox<Object> pathBox = new SuRenFilterBox<Object>();
		final DefaultListModel listModel = new DefaultListModel();
		JList remotePathList = new JList(listModel);
		remotePathList.setVisibleRowCount(5);
		
		pathBox.setToolTipText("Ctrl+L");
		pathBox.registerKeyboardAction(new FocusAndSelectListener(),
				KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		OsgiMgrSetting osgiCfg = loadCfg();
		if(osgiCfg != null)
		{
			Set<String> hisPath = osgiCfg.getHistoryRemote();
			if(hisPath != null)
			{
				for(String path : hisPath)
				{
					pathBox.addItem(path);
				}
				
				pathBox.setSource(Arrays.asList(hisPath.toArray()));
			}
		}
		
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
		
		pathBox.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				String cmd = e.getActionCommand();
				
				if("comboBoxEdited".equals(cmd) && source instanceof JComboBox
						&& remote.isSelected())
				{
					String path = ((JComboBox) source).getSelectedItem().toString();
					
					OsgiMgrSetting osgiCfg = loadCfg();
					osgiCfg.addHistoryRemote(path);
					saveCfg(osgiCfg);
					
					data.add(path);
					
					if(!listModel.contains(path))
					{
						listModel.addElement(path);
					}
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
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		listPanel.add(remotePathList);
		
		JPanel pathPanel = new JPanel();
		pathPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pathPanel.setBackground(Color.BLACK);
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
		pathPanel.add(pathBox);
		pathPanel.add(listPanel);
		
		JPanel remotePanel = new JPanel();
		remotePanel.setLayout(new BoxLayout(remotePanel, BoxLayout.X_AXIS));
		remotePanel.add(pathPanel);
		remotePanel.add(remote);
		
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
	
	private JToolBar createCmdBar(Set<String> data)
	{
		JToolBar cmdBar = new JToolBar();
		cmdBar.setVisible(false);
		cmdBar.setLayout(new BorderLayout());
		
		JTextArea outputArea = new JTextArea();
		JScrollPane outputScroll = new JScrollPane(outputArea);
		SuRenFilterBox<Object> cmdField = new SuRenFilterBox<Object>();
		final JButton killBut = new JButton("Kill");
		final JSchConnection connection = new JSchConnection();
		
		cmdBar.add(outputScroll, BorderLayout.CENTER);
		
		JPanel cmdPanel = new JPanel();
		cmdPanel.setLayout(new BorderLayout());
		cmdPanel.add(cmdField, BorderLayout.CENTER);
		cmdPanel.add(killBut, BorderLayout.EAST);
		
		cmdBar.add(cmdPanel, BorderLayout.SOUTH);
		
		killBut.setEnabled(false);
		outputArea.setEditable(false);
		outputArea.setRows(10);
		
		final JTextAreaAppender areaAppender = new JTextAreaAppender();
		areaAppender.setTargetArea(outputArea);
		
		OsgiMgrSetting cmdSetting = loadCfg();
		if(cmdSetting != null)
		{
			Set<String> historyCmd = cmdSetting.getHistoryCmd();
			
			if(historyCmd != null)
			{
				cmdField.setSource(Arrays.asList(historyCmd.toArray()));
			}
		}
		
		cmdField.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				Object source = e.getSource();
				if(!(source instanceof JTextField))
				{
					return;
				}
				
				final JTextField cmdField = (JTextField) source;
				cmdField.setEnabled(false);
				killBut.setEnabled(true);
				
				new Thread()
				{

					@Override
					public void run()
					{
						try
						{
							String cmd = e.getActionCommand();
							
							executeCmd(cmd, connection, areaAppender);
						}
						finally
						{
							cmdField.setEnabled(true);
							killBut.setEnabled(false);
						}
					}
				}.start();
			}
		});
		
		killBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Channel channel = connection.getChannel();
				if(channel != null)
				{
					channel.disconnect();
				}
			}
		});
		
		return cmdBar;
	}
	
	private JSch jsch = new JSch();
	private Session session = null;
	
	private Session getSession(String user, String host, int port, String pwd)
	{
		if(session != null && session.isConnected())
		{
			return session;
		}
		
		try
		{
			session = jsch.getSession(user, host, port);
			
			SimpleUserInfo userInfo = new SimpleUserInfo(panel);
			userInfo.setPassword(pwd);
			
			session.setUserInfo(userInfo);
			session.connect();
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}

		return session;
	}
	
	private ChannelExec getShell(Session session)
	{
		if(session == null || !session.isConnected())
		{
			return null;
		}
		
		try
		{
			return (ChannelExec) session.openChannel("exec");
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String executeCmd(String cmd, JSchConnection connection,
			JTextAreaAppender areaAppender)
	{
		StringBuffer buffer = new StringBuffer();
		if(cmd == null || "".equals(cmd))
		{
			return buffer.toString();
		}
		
		long beginTime = System.currentTimeMillis();
		OsgiMgrSetting cfg = loadCfg();
		
		String pwd = cfg.getSshPwd();
		int port = cfg.getSshPort();
		String host = cfg.getHost();
		String user = cfg.getSshUser();
		
		Session session = getSession(user, host, port, pwd);
		ChannelExec shell = getShell(session);
		
		if(shell == null)
		{
			System.err.println("open session error.");
			
			return buffer.toString();
		}
		
		if(connection != null)
		{
			connection.setChannel(shell);
		}
		
		InputStream input = null;
		
		try
		{
			shell.setCommand(cmd);
			shell.setInputStream(null);
			shell.setErrStream(System.err);
			
			input = shell.getInputStream();
			shell.connect();
			
			byte[] buf = new byte[1024];
			int len = -1;
			
			while(true)
			{
				while(input.available() > 0)
				{
					len = input.read(buf);
					
					if(len < 0)
					{
						break;
					}
					
					String tmpBuf = new String(buf, 0, len);
					
					if(areaAppender != null)
					{
						areaAppender.append(tmpBuf);
					}
					
					buffer.append(tmpBuf);
				}
				
				if(shell.isClosed())
				{
					break;
				}
			}
			
			areaAppender.appendLine("--suren--");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IoUtil.closeIo(input);
			shell.disconnect();
		}
		
		long endTime = System.currentTimeMillis();
		updateStatus(endTime - beginTime, "ms");
		
		return buffer.toString();
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
	
	protected int controlOsgiBundle(SuRenTable table, int state)
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
			Object value = table.getValueAt(row, HEAD_ID);
			
			try
			{
				ids[i] = Long.parseLong(value.toString());
			}
			catch(NumberFormatException e)
			{
				ids[i] = -1;
			}
		}
		
		ClientProxyFactoryBean factory = null;
		
		try
		{
			factory = getClientProxy();
		}
		catch(SuRenSettingException e)
		{
		}
		
		if(factory == null)
		{
			return count;
		}
		
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

	private SuRenTable createCenter(SuRenTable table)
	{
		table.setHeaders(HEAD_ID, HEAD_NAME, HEAD_VERSION, HEAD_STATE);
		table.setColumnSorterClass(0, Number.class);
		
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

	private JPanel createDetailInfoTable(SuRenTable table)
	{
		final JPanel panel = new JPanel();
		final SuRenTable detailInfoTable = new SuRenTable();
		final JScrollPane detailInfoScroll = new JScrollPane(detailInfoTable);
		
		detailInfoTable.setHeaders(HEAD_NAME, HEAD_VALUE);
		
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
					
					Object idValue = table.getValueAt(id, HEAD_ID);
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
				@SuppressWarnings("unchecked")
				Vector<Object>[] data = new Vector[6];
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				data[0] = convertToVector("Id:", bundle.getId());
				data[1] = convertToVector("LastModified:", format.format(bundle.getLastModified()));
				data[2] = convertToVector("State:", bundle.getState());
				data[3] = convertToVector("Location:", bundle.getLocation());
				data[4] = convertToVector("StartLevel:", bundle.getLevel());
				
				String headersStr = Arrays.toString(bundle.getHeaders());
				headersStr = headersStr.replace(";, ", "<br/>");
				headersStr = headersStr.replace("[", "");
				headersStr = headersStr.replace("]", "");
				data[5] = convertToVector("Headers:", "<html>" + headersStr + "</html>");
				
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

	private void loadOsgiInfo(SuRenTable osgiTable)
	{
		loadOsgiInfo(osgiTable, null);
	}
	
	private void loadOsgiInfo(SuRenTable osgiTable, String filterStr)
	{
		loadOsgiInfo(osgiTable, filterStr, true);
	}
	
	private void loadOsgiInfo(SuRenTable osgiTable, String filterStr, boolean reload)
	{
		if(osgiTable == null)
		{
			return;
		}
		
		if(reload)
		{
			clearTable(osgiTable);
		}
		
		ClientProxyFactoryBean factory = null;
		
		try
		{
			factory = getClientProxy();
		}
		catch(SuRenSettingException e)
		{
			e.printStackTrace();
		}
		
		if(factory == null)
		{
			return;
		}
		
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
		
		BundleState bundleState = new BundleState();
		
		int len = bundles.size();
		@SuppressWarnings("unchecked")
		Vector<Object>[] items = new Vector[len];
		for(int i = 0; i < len; i++)
		{
			SuRenBundle bundle = bundles.get(i);
			Vector<Object> item = new Vector<Object>();
			
			int state = bundle.getState();
			
			item.add(bundle.getId());
			item.add(bundle.getName());
			item.add(bundle.getVersion());
			item.add(getDisplay(state, bundleState));
			
			items[i] = item;
		}
		fillTable(osgiTable, items);
		
		MainFrame main = MainFrame.getInstance();
		main.setTitle("BundleInfo: ",
				bundles.size(), " in total, ",
				bundleState.getActiveNum(), " in active, ",
				bundleState.getInstalledNum(), " in installed, ",
				bundleState.getResolvedNum(), " in resolved");
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
	
	private ClientProxyFactoryBean getClientProxy()
	{
		String url = setting.getUrl();
		int port = setting.getPort();
		if(url == null || "".equals(url) || port < 0 || port > 65536)
		{
			throw new SuRenSettingException("webservice url or port is invalid.");
		}
		
		String host = setting.getUrl() + ":" + setting.getPort() + "/bundle";
		if(!host.startsWith("http://") && !host.startsWith("https://"))
		{
			host = "http://" + host;
		}
		
		return getClientProxy(host, BundleServer.class);
	}
	
	private ClientProxyFactoryBean getClientProxy(String url, Class<BundleServer> clazz)
	{
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		if(url == null || "".equals(url))
		{
			throw new SuRenSettingException("webservice url is invalid.");
		}
		
		factory.setAddress(url);
		factory.setServiceClass(BundleServer.class);
		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		
		return factory;
	}

	private void fillTable(SuRenTable table, Vector<Object> ... datas)
	{
		fillTable(table, false, datas);
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
	
	private String getDisplay(int code, BundleState bundleState)
	{
		String display = "";
		if(bundleState == null)
		{
			bundleState = new BundleState();
		}
		
		switch(code)
		{
			case SuRenBundle.UNINSTALLED:
				display = "uninstalled";
				break;
			case SuRenBundle.INSTALLED:
				display = "installed";
				bundleState.increaseInstalled();
				break;
			case SuRenBundle.RESOLVED:
				display = "resolved";
				bundleState.increaseResolved();
				break;
			case SuRenBundle.STARTING:
				display = "starting";
				break;
			case SuRenBundle.STOPPING:
				display = "stopping";
				break;
			case SuRenBundle.ACTIVE:
				display = "active";
				bundleState.increaseActive();
				break;
		}
		
		return display;
	}
	
	private OsgiMgrSetting getOsgiCfg(String path)
	{
		SettingUtil<OsgiMgrSetting> settingUtil = new SettingUtil<OsgiMgrSetting>();
		
		OsgiMgrSetting data = settingUtil.load(path, OsgiMgrSetting.class);
		
		return data;
	}

	@Override
	protected OsgiMgrSetting loadCfg()
	{
		return getOsgiCfg(OSGI_CFG_PATH);
	}
	
	private boolean saveOsgiCfg(String path, OsgiMgrSetting osgiCfg)
	{
		SettingUtil<OsgiMgrSetting> settingUtil = new SettingUtil<OsgiMgrSetting>();
		
		return settingUtil.save(osgiCfg, path, OsgiMgrSetting.class);
	}

	@Override
	protected boolean saveCfg(OsgiMgrSetting cfgObj)
	{
		return saveOsgiCfg(OSGI_CFG_PATH, cfgObj);
	}
	
	class Setting
	{
		private String url;
		private int port;
		private String sshUser;
		private String sshPassword;
		private int sshPort;
		private int outputBuf;

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

		public String getSshUser()
		{
			return sshUser;
		}

		public void setSshUser(String sshUser)
		{
			this.sshUser = sshUser;
		}

		public String getSshPassword()
		{
			return sshPassword;
		}

		public void setSshPassword(String sshPassword)
		{
			this.sshPassword = sshPassword;
		}

		public int getSshPort()
		{
			return sshPort;
		}

		public void setSshPort(int sshPort)
		{
			this.sshPort = sshPort;
		}

		public int getOutputBuf()
		{
			return outputBuf;
		}

		public void setOutputBuf(int outputBuf)
		{
			this.outputBuf = outputBuf;
		}
	}
	
	class BundleState
	{
		private int activeNum = 0;
		private int installedNum = 0;
		private int resolvedNum = 0;
		public int increaseActive()
		{
			return activeNum++;
		}
		public int getActiveNum()
		{
			return activeNum;
		}
		public int increaseInstalled()
		{
			return installedNum++;
		}
		public int getInstalledNum()
		{
			return installedNum;
		}
		public int increaseResolved()
		{
			return resolvedNum++;
		}
		public int getResolvedNum()
		{
			return resolvedNum;
		}
	}
}