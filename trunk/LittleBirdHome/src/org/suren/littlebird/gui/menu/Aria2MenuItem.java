package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.GeneralPanel;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenComboBox;
import org.suren.littlebird.gui.SuRenFilterBox;
import org.suren.littlebird.gui.SuRenTable;
import org.suren.littlebird.gui.SuRenTableModel;
import org.suren.littlebird.gui.TabInfo;
import org.suren.littlebird.io.HttpConnect;

@Menu(displayName = "Aria2", parentMenu = DownloadMenu.class, index = 0)
public class Aria2MenuItem extends ArchMenu<Object>
{
	private JTabbedPane	panel;
	private JPopupMenu tabPopuMenu = null;

	private final String ACTIVE_TABLE = "activeTable";
	private final String STATUS_LABEL = "status";
	private final String SHOW_TYPE = "show_type";

	private final Map<String, Integer> typeMap = new HashMap<String, Integer>();

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
				main.getContentPanel().add("Aria2", panel);

				init();
			}

			main.getContentLayout().show(main.getContentPanel(), "Aria2");

			main.reDrawPanel();
		}
	};

	private void init()
	{
		if(tabPopuMenu != null)
		{
			return;
		}

		int index = 0;

		typeMap.put("active", index++);
		typeMap.put("waiting", index++);
		typeMap.put("stopped", index++);
		typeMap.put("all", index++);

		tabPopuMenu = createTabMenu();

		addTab(new TabInfo());
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

		return menu;
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

		innerPanel.add(createToolBar(tabInfo), BorderLayout.NORTH);
		innerPanel.add(createCenterZone(tabInfo), BorderLayout.CENTER);
		innerPanel.add(createStatusPanel(tabInfo), BorderLayout.SOUTH);

		String title = tabInfo.getTitle();
		if("".equals(title) || title == null)
		{
			title = "suren";
		}

		panel.addTab(title, innerPanel);
		panel.setSelectedComponent(innerPanel);
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private Component createStatusPanel(TabInfo tabInfo)
	{
		JLabel statusLabel = new JLabel("status:");

		tabInfo.putItem(STATUS_LABEL, statusLabel);

		return statusLabel;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private Component createToolBar(final TabInfo tabInfo)
	{
		JToolBar toolbar = new JToolBar();
		final JToolBar addTaskBar = createAddTaskBar(tabInfo);

		final SuRenComboBox typeBox = new SuRenComboBox();
		for(String type : typeMap.keySet())
		{
			typeBox.addItem(type);

			if("active".equals(type))
			{
				typeBox.setSelectedItem(type);
			}
		}

		JButton reloadBut = new JButton("Reload");
		reloadBut.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent actionevent)
			{
				tabInfo.putItem(SHOW_TYPE, typeBox.getSelectedItem());

				taskLoad(tabInfo);

				globalStatLoad(tabInfo);
			}
		});

		JButton addBut = new JButton("Add");
		addBut.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent actionevent)
			{
				addTaskBar.setVisible(!addTaskBar.isVisible());
			}
		});

		SuRenFilterBox<String> filterBox = new SuRenFilterBox<String>();
		SuRenComboBox serverList = new SuRenComboBox();

		String server = "http://192.168.18.203:8080";
		serverList.addItem(server);

		toolbar.add(typeBox);
		toolbar.add(reloadBut);
		toolbar.add(addBut);
		toolbar.add(filterBox);
		toolbar.add(serverList);

		tabInfo.setServer(server);

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolbar, BorderLayout.NORTH);
		toolBarPanel.add(addTaskBar, BorderLayout.CENTER);

		return toolBarPanel;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private JToolBar createAddTaskBar(final TabInfo tabInfo)
	{
		JToolBar addTaskBar = new JToolBar();
		addTaskBar.setVisible(false);

		JLabel urlLabel = new JLabel("url:");
		final JTextField urlField = new JTextField();
		JButton submitBut = new JButton("submit");

		addTaskBar.add(urlLabel);
		addTaskBar.add(urlField);
		addTaskBar.add(submitBut);

		submitBut.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent actionevent)
			{
				String url = urlField.getText();

				submitUrl(tabInfo, url);
			}
		});

		return addTaskBar;
	}

	/**
	 * @param url
	 */
	protected boolean submitUrl(TabInfo tabInfo, String url)
	{
		if(tabInfo == null || url == null)
		{
			return false;
		}

		HttpConnect httpConnect = new HttpConnect();

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("url", url);

		String result = httpConnect.post(tabInfo.getServer() + "/add", data);

		System.out.println(result);

		return true;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private Component createCenterZone(TabInfo tabInfo)
	{
		JPanel cardPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();

		cardPanel.setLayout(cardLayout);

		Component activeTaskPanel = createTaskPanel(tabInfo);

		cardPanel.add("activeTask", activeTaskPanel);

		return cardPanel;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private Component createTaskPanel(TabInfo tabInfo)
	{
		if(tabInfo == null)
		{
			return null;
		}

		SuRenTable activeTable = new SuRenTable();
		final JPopupMenu taskPopupMenu = createTaskMenu(tabInfo);

		activeTable.setHeaders("index", "name", "gid", "path", "status", "completed",
				"total", "connections", "downloadSpeed", "pieces", "persent");

		activeTable.addMouseListener(new MouseAdapter()
		{

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				int button = mouseEvent.getButton();
				Object sourceObj = mouseEvent.getSource();
				if(!(sourceObj instanceof SuRenTable))
				{
					return;
				}

				SuRenTable table = (SuRenTable) sourceObj;

				if(button == MouseEvent.BUTTON3)
				{
					taskPopupMenu.show(table, mouseEvent.getX(), mouseEvent.getY());
				}
			}
		});

		JScrollPane activeTableScrool = new JScrollPane(activeTable);
		tabInfo.putItem(ACTIVE_TABLE, activeTable);

		boolean result = taskLoad(tabInfo);

		return activeTableScrool;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private JPopupMenu createTaskMenu(final TabInfo tabInfo)
	{
		JPopupMenu menu = new JPopupMenu("task");
		if(tabInfo == null)
		{
			return menu;
		}

		JMenuItem pauseItem = new JMenuItem("Pause");
		JMenuItem pauseAllItem = new JMenuItem("PauseAll");
		JMenuItem unPauseItem = new JMenuItem("UnPause");
		JMenuItem unPauseAllItem = new JMenuItem("UnPauseAll");
		JMenuItem removeItem = new JMenuItem("Remove");

		menu.add(pauseItem);
		menu.add(pauseAllItem);
		menu.add(unPauseItem);
		menu.add(unPauseAllItem);
		menu.add(removeItem);

		pauseItem.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent actionevent)
			{
				SuRenTable table = getTable(tabInfo);
				if(table == null)
				{
					return;
				}

				int[] rows = table.getSelectedRows();
				if(rows == null)
				{
					return;
				}

				for(int row : rows)
				{
					Object value = table.getValueAt(row, "gid");

					pause(tabInfo, value.toString());
				}

			}
		});

		unPauseItem.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent actionevent)
			{
				SuRenTable table = getTable(tabInfo);
				if(table == null)
				{
					return;
				}

				int[] rows = table.getSelectedRows();
				if(rows == null)
				{
					return;
				}

				for(int row : rows)
				{
					Object value = table.getValueAt(row, "gid");

					unpause(tabInfo, value.toString());
				}
			}
		});

		return menu;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private boolean taskLoad(TabInfo tabInfo)
	{
		Object type = null;
		SuRenTable table = getTable(tabInfo);
		if(table == null || (type = tabInfo.getItem().get(SHOW_TYPE)) == null)
		{
			return false;
		}

		cleanTable(table);

		int typeCode = typeMap.get(type);
		List<String> urls = new ArrayList<String>();

		switch(typeCode)
		{
			case 0:
				urls.add(tabInfo.getServer() + "/active");
				break;
			case 1:
				urls.add(tabInfo.getServer() + "/waiting");
				break;
			case 2:
				urls.add(tabInfo.getServer() + "/stopped");
				break;
			case 3:
				urls.add(tabInfo.getServer() + "/active");
				urls.add(tabInfo.getServer() + "/waiting");
				urls.add(tabInfo.getServer() + "/stopped");
				break;
		}

		for(String url : urls)
		{
			typeTaskLoad(table, url);
		}

		return true;
	}

	/**
	 * @param table
	 * @param url
	 */
	private void typeTaskLoad(SuRenTable table, String url)
	{
		if(table == null || url == null)
		{
			return;
		}

		String response = new HttpConnect().request(url);
		if(response == null)
		{
			return;
		}

		JSONArray jsonObj = JSONArray.fromObject(response);

		for(int i = 0; i < jsonObj.size(); i++)
		{
			JSONObject obj = jsonObj.getJSONObject(i);
			Vector<Object> data = new Vector<Object>();

			data.add(i);

			String path = obj.getJSONArray("files").getJSONObject(0).get("path").toString();
			data.add(new File(path).getName());

			long completedLen = obj.getLong("completedLength");
			long totalLen = obj.getLong("totalLength");
			String persent = String.valueOf(100.0 * completedLen / totalLen);

			if(persent.indexOf(".") != -1)
			{
				int len = persent.indexOf(".") + 3;
				if(len > persent.length())
				{
					len = persent.length();
				}

				persent = persent.substring(0, len) + "%";
			}

			data.add(obj.get("gid"));
			data.add(obj.get("dir"));
			data.add(obj.get("status"));
			data.add(completedLen);
			data.add(totalLen);
			data.add(obj.get("connections"));
			data.add(obj.get("downloadSpeed"));
			data.add(obj.get("numPieces"));
			data.add(persent);

			fillTable(table, data);
		}
	}

	private boolean globalStatLoad(TabInfo tabInfo)
	{
		JLabel statusLabel = getStatus(tabInfo);
		if(statusLabel == null)
		{
			return false;
		}

		String server = tabInfo.getServer();
		String url = (server + "/stat");
		String resposne = new HttpConnect().request(url);
		if(resposne == null)
		{
			return false;
		}

		statusLabel.setText(resposne);

		return true;
	}

	private boolean pause(TabInfo tabInfo, String gid)
	{
		if(tabInfo == null || gid == null)
		{
			return false;
		}

		String server = tabInfo.getServer();
		String url = (server + "/pause?gid="+gid);
		String resposne = new HttpConnect().request(url);

		System.out.println(resposne);

		return true;
	}

	private boolean unpause(TabInfo tabInfo, String gid)
	{
		if(tabInfo == null || gid == null)
		{
			return false;
		}

		String server = tabInfo.getServer();
		String url = (server + "/unpause?gid="+gid);
		String resposne = new HttpConnect().request(url);

		System.out.println(resposne);

		return true;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private SuRenTable getTable(TabInfo tabInfo)
	{
		Object tabSrc = null;
		SuRenTable table = null;
		Map<String, Object> item = null;
		if(tabInfo == null || (item = tabInfo.getItem()) == null)
		{
			return null;
		}

		tabSrc = item.get(ACTIVE_TABLE);
		if(!(tabSrc instanceof SuRenTable) || (table = (SuRenTable) tabSrc) == null)
		{
			return null;
		}

		return table;
	}

	/**
	 * @param tabInfo
	 * @return
	 */
	private JLabel getStatus(TabInfo tabInfo)
	{
		Object labelSrc = null;
		JLabel statusLabel = null;
		Map<String, Object> item = null;
		if(tabInfo == null || (item = tabInfo.getItem()) == null)
		{
			return null;
		}

		labelSrc = item.get(STATUS_LABEL);
		if(!(labelSrc instanceof JLabel) || (statusLabel = (JLabel) labelSrc) == null)
		{
			return null;
		}

		return statusLabel;
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

	private void cleanTable(SuRenTable table)
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
	protected boolean saveCfg(Object cfgObj)
	{
		return false;
	}

	@Override
	protected Object loadCfg()
	{
		return null;
	}
}
