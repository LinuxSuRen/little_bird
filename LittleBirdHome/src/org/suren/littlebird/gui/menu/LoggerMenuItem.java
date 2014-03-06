package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.log4j.Level;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.FocusAndSelectListener;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenTable;
import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.net.webservice.ClientProxy;
import org.suren.littlebird.server.LoggerServer;
import org.suren.littlebird.setting.LoggerMgrSetting;
import org.suren.littlebird.setting.OsgiMgrSetting;
import org.suren.littlebird.setting.SettingUtil;

@Menu(displayName = "Logger", parentMenu = RemoteMenu.class, index = 2,
		keyCode = KeyEvent.VK_L, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class LoggerMenuItem extends ArchMenu<LoggerMgrSetting>
{
	private final String	LOGGER_CFG_PATH	= "logger_mgr_cfg.xml";
	private final String	HEAD_NAME		= "name";
	private final String	HEAD_LEVEL		= "level";
	
	protected JPanel	panel;
	
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
		SuRenTable table = new SuRenTable();
		createToolBar(table);
		createCenter(table);
	}

	private void createToolBar(final SuRenTable table)
	{
		JToolBar controlBar = new JToolBar();

		final JComboBox levelBox = new JComboBox();
		JButton setLevelBut = new JButton("SetLevel");
		final JComboBox filterBox = new JComboBox();
		final JButton reloadBut = new JButton("Reload");
		
		controlBar.add(levelBox);
		controlBar.add(setLevelBut);
		controlBar.addSeparator();
		controlBar.add(filterBox);
		controlBar.add(reloadBut);
		
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
				
				reloadBut.doClick();
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
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		controlPanel.add(controlBar, BorderLayout.NORTH);
		
		panel.add(controlPanel, BorderLayout.NORTH);
	}

	private void createCenter(SuRenTable table)
	{
		table.setHeaders(HEAD_NAME, HEAD_LEVEL);
		
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private void reload(SuRenTable table)
	{
		reload(table, null, true);
	}
	
	private void reload(SuRenTable table, String filter)
	{
		reload(table, filter, true);
	}
	
	private void reload(SuRenTable table, String filter, boolean reload)
	{
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
			
			item.add(name);
			item.add(loggerServer.getLevel(name));
			
			items[i] = item;
		}
		fillTable(table, items);
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
		return false;
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
