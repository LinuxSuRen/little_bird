package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.ConnectButton;
import org.suren.littlebird.gui.GeneralDropTarget;
import org.suren.littlebird.gui.GeneralPanel;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenTable;
import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.net.HomeScp;
import org.suren.littlebird.net.ssh.SimpleSftpProgressMonitor;
import org.suren.littlebird.net.ssh.SimpleUserInfo;
import org.suren.littlebird.setting.OsgiMgrSetting;
import org.suren.littlebird.setting.SettingUtil;
import org.suren.littlebird.setting.SshSetting;
import org.suren.littlebird.setting.SshSetting.Ssh;
import org.suren.littlebird.thread.GeneralThread;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Menu(displayName = "Scp", parentMenu = RemoteMenu.class, index = 0,
		keyCode = KeyEvent.VK_P, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class ScpMenuItem extends ArchMenu<SshSetting>
{
	private JTabbedPane panel = null;
	private JPopupMenu tabPopuMenu = null;
	private JPopupMenu localPopupMenu = null;
	private JSch jsch = new JSch();
	private List<Session> sesssionList = new ArrayList<Session>();
	
	private final String HEAD_PATH = "path";
	private final String HEAD_SIZE = "size";
	private final String HEAD_TYPE = "type";
	private final String HEAD_LEVEL = "level";
	private final String HEAD_TIME = "time";
	private final String HEAD_RESULT = "result";
	
	private final String SSH_CFG_PATH = "ssh_cfg.xml";
	
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			MainFrame main = MainFrame.getInstance();
			if(panel == null)
			{
				panel = new JTabbedPane();
				main.getContentPanel().add("Scp", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Scp");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
		if(tabPopuMenu != null)
		{
			return;
		}
		
		tabPopuMenu = createTabMenu();
		
		addTab(new TabInfo());
		
		panel.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int butCode = e.getButton();
				int index = panel.indexAtLocation(e.getX(), e.getY());
				if(index == -1)
				{
					if(2 == e.getClickCount())
					{
						addTab(new TabInfo());
					}
					else
					{
						return;
					}
				}
				
				if(butCode == MouseEvent.BUTTON3)
				{
					tabPopuMenu(index, e.getX(), e.getY());
				}
			}
		});
	}
	
	private JPopupMenu createTabMenu()
	{
		final JPopupMenu menu = new JPopupMenu();

		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem closeItem = new JMenuItem("Close");
		JMenuItem duplicateItem = new JMenuItem("Duplicate");

		menu.add(saveItem);
		menu.add(closeItem);
		menu.add(duplicateItem);
		
		saveItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JTabbedPane pane;
				Component selectedComponent;
				GeneralPanel<TabInfo> generalPanel;
				Component invoker = menu.getInvoker();
				if(!(invoker instanceof JTabbedPane))
				{
					return;
				}
				
				pane = (JTabbedPane) invoker;
				selectedComponent = pane.getSelectedComponent();
				if(!(selectedComponent instanceof GeneralPanel))
				{
					return;
				}
				
				generalPanel = (GeneralPanel<TabInfo>) selectedComponent;
				TabInfo data = generalPanel.getDataObject();
				
				if(data == null || data.getHost() == null || "".equals(data.getHost()))
				{
					return;
				}
				
				SshSetting setting = loadCfg();
				if(setting == null)
				{
					setting = new SshSetting(); 
				}
				setting.setLastHost(data.getHost());

				SshSetting.Ssh ssh = new SshSetting.Ssh();
				ssh.setAlias(data.getUser());
				ssh.setUser(data.getUser());
				ssh.setPort(data.getPort());
				ssh.setHost(data.getHost());
				setting.addSsh(ssh);
				
				System.out.println(saveCfg(setting));
			}
		});
		
		duplicateItem.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JTabbedPane pane;
				Component selectedComponent;
				GeneralPanel<TabInfo> generalPanel;
				Component invoker = menu.getInvoker();
				if(!(invoker instanceof JTabbedPane))
				{
					return;
				}
				
				pane = (JTabbedPane) invoker;
				selectedComponent = pane.getSelectedComponent();
				if(!(selectedComponent instanceof GeneralPanel))
				{
					return;
				}
				
				generalPanel = (GeneralPanel<TabInfo>) selectedComponent;
				
				addTab(generalPanel.getDataObject());
			}
		});
		
		return menu;
	}

	protected void tabPopuMenu(int index, int x, int y)
	{
		tabPopuMenu.show(panel, x, y);
	}

	private void addTab(TabInfo tabInfo)
	{
		if(tabInfo == null)
		{
			return;
		}
		
		GeneralPanel<TabInfo> innerPanel = new GeneralPanel<TabInfo>();
		innerPanel.setDataObject(tabInfo);
		innerPanel.setLayout(new BorderLayout());

		innerPanel.add(createCenterZone(tabInfo), BorderLayout.CENTER);
		innerPanel.add(createToolBar(tabInfo), BorderLayout.NORTH);
		innerPanel.add(createLogPanel(tabInfo), BorderLayout.SOUTH);
		
		String title = tabInfo.getTitle();
		if("".equals(title) || title == null)
		{
			title = "suren";
		}
		
		panel.addTab(title, innerPanel);
		panel.setSelectedComponent(innerPanel);
	}

	private JToolBar createToolBar(final TabInfo tabInfo)
	{
		final JToolBar toolBar = new JToolBar();
		
		final JButton syncBut = new JButton("sync");
		final JTextField userField = new JTextField(tabInfo.getUser());
		final JTextField hostField = new JTextField(tabInfo.getHost());
		final JTextField portField = new JTextField(tabInfo.getPort() + "");
		final JTextField pwdField = new JPasswordField(tabInfo.getPassword());
		final ConnectButton conBut = new ConnectButton();
		
		conBut.setUser(userField);
		conBut.setHost(hostField);
		conBut.setPort(portField);
		conBut.setPassword(pwdField);
		
		syncBut.setMnemonic('s');
		conBut.setMnemonic('c');

		toolBar.add(syncBut);
		toolBar.add(userField);
		toolBar.add(hostField);
		toolBar.add(portField);
		toolBar.add(pwdField);
		toolBar.add(conBut);
		
		syncBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GeneralThread<TabInfo> syncThread = new GeneralThread<TabInfo>("Sync")
				{

					@Override
					public void run()
					{
						syncBut.setEnabled(false);
						sync();
						syncBut.setEnabled(true);
					}
					
					private void sync()
					{
						TabInfo tabInfo = getData();
						List<String> localFiles = tabInfo.getLocalFiles();
						ChannelSftp ftp = tabInfo.getFtpChannel();
						
						if(localFiles == null || localFiles.size() == 0)
						{
							return;
						}
						
						if(ftp.isClosed())
						{
							return;
						}
						
						for(String path : localFiles)
						{
							File file = new File(path);
							
							try
							{
								ftp.put(path, file.getName(), tabInfo.getMonitor());
							}
							catch (SftpException e1)
							{
								e1.printStackTrace();
							}
						}
					}
				};
				syncThread.setData(tabInfo);
				syncThread.start();
			}
		});
		
		conBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(conBut.isConnected())
				{
					return;
				}
				
				Object source = e.getSource();
				if(!(source instanceof ConnectButton))
				{
					return;
				}
				
				ConnectButton connect = (ConnectButton) source;
				
				String user = connect.getUser().getText();
				String host = connect.getHost().getText();
				String port = connect.getPort().getText();
				String pwd = connect.getPassword().getText();
				
				Session session = openSession(user, host, port, pwd);
				if(session != null && session.isConnected())
				{
					userField.setEnabled(false);
					hostField.setEnabled(false);
					portField.setEnabled(false);
					pwdField.setEnabled(false);
					
					tabInfo.setSession(session);
				}
				else
				{
					return;
				}
				
				if(tabInfo.getTitle() == null || "".equals(tabInfo.getTitle()))
				{
					tabInfo.setTitle(host);
				}
				
				tabInfo.setUser(user);
				tabInfo.setHost(host);
				tabInfo.setPassword(pwd);
				conBut.setConnected(true);
				
				panel.setTitleAt(panel.getSelectedIndex(), tabInfo.getTitle());
				
				try
				{
					ChannelSftp ftp = (ChannelSftp) session.openChannel("sftp");
					
					ftp.connect();
					tabInfo.setFtpChannel(ftp);
					
					if(!"".equals(tabInfo.getPath()))
					{
						ftp.cd(tabInfo.getPath());
					}
					
					connect.setData("channel", ftp);
					
					remoteFlush(tabInfo);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		
		return toolBar;
	}
	
	private Component createLogPanel(TabInfo tabInfo)
	{
		JToolBar logBar = new JToolBar();
		
		JTextArea logArea = new JTextArea();
		JScrollPane logScroll = new JScrollPane(logArea);
		
		logBar.add(logScroll);
		
		return logBar;
	}
	
	protected void remoteFlush(TabInfo tabInfo)
	{
		ChannelSftp channel = null;
		if(tabInfo == null || (channel = tabInfo.getFtpChannel()) == null
				|| channel.isClosed())
		{
			return;
		}
		
		SuRenTable table = tabInfo.getRemoteTable();
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
		
		try
		{
			Vector list = channel.ls(".");
			
			Collections.sort(list);
			
			for(int i = 0; i < list.size(); i++)
			{
				Object entry = list.get(i);
				
				if(!(entry instanceof LsEntry))
				{
					continue;
				}
				
				LsEntry lsEntry = (LsEntry) entry;
				
				Vector<Object> data = new Vector<Object>();
				data.add(lsEntry.getFilename());
				
				fillTable(table, data);
			}
		}
		catch (SftpException e)
		{
			e.printStackTrace();
		}
	}

	private void fillTable(SuRenTable table, Vector<Object> ... datas)
	{
		if(table == null || datas == null)
		{
			return;
		}
		
		SuRenTableModel model = table.getModel();
		for(Vector<Object> data : datas)
		{
			model.addRow(data);
		}
	}
	
	protected Session openSession(String user, String host, String port, String pwd)
	{
		Session session = null;
		
		try
		{
			int size = sesssionList.size();
			
			for(int i = 0; i < size; i++)
			{
				Session item = sesssionList.get(i);
				
				if(!item.isConnected())
				{
					sesssionList.remove(i);
					i--;
					size--;
					continue;
				}
				
				if(item.getHost().equals(host))
				{
					session = item;
					
					break;
				}
			}
			
			if(session == null)
			{
				session = jsch.getSession(user, host, 22);
				
				SimpleUserInfo userInfo = new SimpleUserInfo(panel);
				userInfo.setPassword(pwd);
				session.setUserInfo(userInfo);
				
				session.connect();
			}
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		
		return session;
	}

	private Component createCenterZone(final TabInfo tabInfo)
	{
		JSplitPane pane = new JSplitPane();
		pane.setOneTouchExpandable(true);
		pane.setDividerLocation((int)((MainFrame.getInstance().getContentPanel().getWidth() - pane.getDividerSize()) * 0.5));
		
		SuRenTable localTable = new SuRenTable();
		localTable.setHeaders(HEAD_PATH, HEAD_SIZE, HEAD_LEVEL);
		localPopupMenu = createLocalPopupMenu(tabInfo);
		localTable.addKeyListener(new KeyAdapter()
		{

			@Override
			public void keyReleased(KeyEvent e)
			{
				Object source = e.getSource();
				if(!(source instanceof SuRenTable))
				{
					return;
				}
				
				int code = e.getKeyCode();
				if(code == KeyEvent.VK_DELETE)
				{
					SuRenTable table = (SuRenTable) source;
					int[] rows = table.getSelectedRows();
					TableModel tbModel = table.getModel();
					DefaultTableModel model;
					if(tbModel instanceof DefaultTableModel)
					{
						model = (DefaultTableModel) tbModel;
					}
					else
					{
						return;
					}
					
					Arrays.sort(rows);
					
					for(int i = rows.length - 1; i >= 0; i--)
					{
						int row = rows[i];
						
						tabInfo.getLocalFiles().remove(model.getValueAt(row, 0));
						model.removeRow(row);
					}
				}
			}
		});
		localTable.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int buttonCode = e.getButton();
				Object source = e.getSource();
				
				if(MouseEvent.BUTTON3 == buttonCode
						&& source instanceof SuRenTable)
				{
					localPopupMenu.show((SuRenTable) source, e.getX(), e.getY());
				}
			}
		});
		
		preLoadFiles(localTable, tabInfo);
		
		JScrollPane localPane = new JScrollPane(localTable);
		GeneralDropTarget<SuRenTable> localDropTarget = new GeneralDropTarget<SuRenTable>()
		{
			private static final long	serialVersionUID	= 8863733190573710775L;

			@Override
			public synchronized void drop(DropTargetDropEvent drop)
			{
				if(getTargetObject() == null)
				{
					return;
				}
				
				drop.acceptDrop(DnDConstants.ACTION_REFERENCE);
				Transferable transferable = drop.getTransferable();
				
				try
				{
					Object transfData = transferable.getTransferData(DataFlavor.javaFileListFlavor);
					List<File> result = null;
					if(transfData == null || !(transfData instanceof List))
					{
						return;
					}
					
					
					result = (List<File>)transfData;
					for(File file : result)
					{
						Vector<Object> data = new Vector<Object>();
						
						data.add(file.getAbsolutePath());
						data.add(file.length());
						
						tabInfo.getLocalFiles().add(file.getAbsolutePath());
						
						fillTable(getTargetObject(), data);
					}
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
		};
		localDropTarget.setTargetObject(localTable);
		localPane.setDropTarget(localDropTarget);
		localPane.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}
		});
		
		SimpleSftpProgressMonitor<SuRenTable> monitor = new SimpleSftpProgressMonitor<SuRenTable>()
		{
			private long maxCount;
			private long nowCount;
			private AtomicInteger index;
			private AtomicBoolean done = new AtomicBoolean(false);
			private Object lock;
			
			@Override
			public void init(int op, String src, String dest, long max)
			{
				SuRenTable target = getTarget();
				int rowCount = target.getRowCount();
				
				maxCount = max;
				nowCount = 0;
				index = new AtomicInteger(-1);
				
				for(int i = 0; i < rowCount; i++)
				{
					Object value = target.getValueAt(i, 0);
					
					if(src.equals(value))
					{
						index.set(i);
						break;
					}
				}
				
				done.set(false);
				lock = new Object();
				
				GeneralThread<Integer> generalThrad = new GeneralThread<Integer>("SimpleMonitorLevel"){
					
					private SuRenTable target = getTarget();

					@Override
					public void run()
					{
						int row = getData();
						if(row == -1)
						{
							return;
						}
						
						while(!done.get())
						{
							synchronized (lock)
							{
								try
								{
									lock.wait(1000);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								
								String level = String.valueOf(100.0 * nowCount / maxCount) + "%";
								System.out.println(level);
								
								target.setValueAt(level, row, 2);
							}
						}
					}
				};
				generalThrad.setData(index.get());
				generalThrad.start();
			}

			@Override
			public boolean count(long count)
			{
				synchronized (lock)
				{
					nowCount += count;
					
					lock.notifyAll();
				}
				
				return true;
			}

			@Override
			public void end()
			{
				done.set(true);
				
				synchronized (lock)
				{
					lock.notifyAll();
				}
			}
		};
		monitor.setTarget(localTable);
		tabInfo.setMonitor(monitor);
		
		JPanel rightPanel = createRightPanel(tabInfo);
		
		pane.setLeftComponent(localPane);
		pane.setRightComponent(rightPanel);
		
		return pane;
	}
	
	private JPopupMenu createLocalPopupMenu(final TabInfo tabInfo)
	{
		localPopupMenu = new JPopupMenu("Local");
		
		JMenuItem pullFiles = new JMenuItem("pull");
		JMenuItem exploreFiles = new JMenuItem("explore");
		
		localPopupMenu.add(pullFiles);
		localPopupMenu.add(exploreFiles);
		
		pullFiles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				JMenuItem item = null;
				JPopupMenu menu = null;
				Component invoker = null;
				if(!(source instanceof JMenuItem))
				{
					return;
				}
				
				item = (JMenuItem) source;
				menu = (JPopupMenu) item.getParent();
				
				if(!((invoker = menu.getInvoker()) instanceof SuRenTable))
				{
					return;
				}
				
				preLoadFiles((SuRenTable) invoker, tabInfo);
			}
		});
		exploreFiles.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		return localPopupMenu;
	}

	private void preLoadFiles(SuRenTable localTable, TabInfo tabInfo)
	{
		if(localTable == null)
		{
			return;
		}
		
		Set<File> localList = HomeScp.tmpPath;
		for(File file : localList)
		{
			Vector<Object> item = new Vector<Object>();
			
			item.add(file.getAbsolutePath());
			item.add(file.length());
			
			fillTable(localTable, item);
			
			tabInfo.getLocalFiles().add(file.getAbsolutePath());
		}
	}

	private JPanel createRightPanel(final TabInfo tabInfo)
	{
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		
		final JComboBox hisPath = new JComboBox();
		hisPath.setEditable(true);
		
		String path = tabInfo.getPath();
		if(path != null)
		{
			hisPath.addItem(path);
		}
		
		hisPath.addItemListener(new ItemListener()
		{
			
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				Object item = e.getItem();
				
				tabInfo.setPath(item.toString());
			}
		});
		
		SuRenTable remoteTable = new SuRenTable();
		remoteTable.setHeaders(HEAD_PATH);
		tabInfo.setRemoteTable(remoteTable);
		remoteTable.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() != 2)
				{
					return;
				}
				
				Object source = e.getSource();
				if(!(source instanceof SuRenTable))
				{
					return;
				}
				
				SuRenTable table = (SuRenTable) source;
				int row = table.getSelectedRow();
				if(row < 0 || row >= table.getRowCount())
				{
					return;
				}
				
				Object value = table.getValueAt(row, 0);
				ChannelSftp channel = tabInfo.getFtpChannel();
				
				if(channel == null || channel.isClosed())
				{
					return;
				}
				
				try
				{
					channel.cd(value.toString());
					
					remoteFlush(tabInfo);
					
					String pwd = channel.pwd();
					hisPath.setSelectedItem(pwd);
					hisPath.addItem(pwd);
				}
				catch (SftpException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		
		JScrollPane remotePane = new JScrollPane(remoteTable);
		
		rightPanel.add(hisPath, BorderLayout.NORTH);
		rightPanel.add(remotePane, BorderLayout.CENTER);
		
		return rightPanel;
	}
	
	private boolean saveCfg(String path, SshSetting cfgObj)
	{
		SettingUtil<SshSetting> settingUtil = new SettingUtil<SshSetting>();
		
		return settingUtil.save(cfgObj, SSH_CFG_PATH,
				SshSetting.class, SshSetting.Ssh.class);
	}

	@Override
	protected boolean saveCfg(SshSetting cfgObj)
	{
		return saveCfg(SSH_CFG_PATH, cfgObj);
	}
	
	private SshSetting loadCfg(String path)
	{
		SettingUtil<SshSetting> settingUtil = new SettingUtil<SshSetting>();
		
		SshSetting data = settingUtil.load(path, SshSetting.class, SshSetting.Ssh.class);
		
		return data;
	}

	@Override
	protected SshSetting loadCfg()
	{
		return loadCfg(SSH_CFG_PATH);
	}

	class TabInfo
	{
		private String title;
		private String user = "root";
		private String password;
		private String host;
		private int port = 22;
		private String path = "/root";
		private List<String> localFiles = new ArrayList<String>();
		private SuRenTable remoteTable;
		private Session session;
		private ChannelSftp ftpChannel;
		private SimpleSftpProgressMonitor<?> monitor;
		
		public TabInfo()
		{
			monitor = new SimpleSftpProgressMonitor<Object>()
			{

				@Override
				public void init(int op, String src, String dest, long max)
				{
				}

				@Override
				public boolean count(long count)
				{
					return true;
				}

				@Override
				public void end()
				{
				}
			};
		}
		
		public TabInfo(String title)
		{
			this.title = title;
		}
		
		public String getTitle()
		{
			return title;
		}
		public void setTitle(String title)
		{
			this.title = title;
		}
		public String getUser()
		{
			return user;
		}
		public void setUser(String user)
		{
			this.user = user;
		}
		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		public String getHost()
		{
			return host;
		}
		public void setHost(String host)
		{
			this.host = host;
		}
		public int getPort()
		{
			return port;
		}
		public void setPort(int port)
		{
			this.port = port;
		}
		public String getPath()
		{
			return path;
		}
		public void setPath(String path)
		{
			this.path = path;
		}

		public List<String> getLocalFiles()
		{
			return localFiles;
		}

		public void setLocalFiles(List<String> localFiles)
		{
			this.localFiles = localFiles;
		}

		public SuRenTable getRemoteTable()
		{
			return remoteTable;
		}

		public void setRemoteTable(SuRenTable remoteTable)
		{
			this.remoteTable = remoteTable;
		}

		public Session getSession()
		{
			return session;
		}

		public void setSession(Session session)
		{
			this.session = session;
		}

		public ChannelSftp getFtpChannel()
		{
			return ftpChannel;
		}

		public void setFtpChannel(ChannelSftp ftpChannel)
		{
			this.ftpChannel = ftpChannel;
		}

		public SimpleSftpProgressMonitor<?> getMonitor()
		{
			return monitor;
		}

		public void setMonitor(SimpleSftpProgressMonitor<?> monitor)
		{
			this.monitor = monitor;
		}
	}
}
