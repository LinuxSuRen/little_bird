package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
	private static final String	HEAD_LEVEL	= "id";
	private static final String	HEAD_SIZE	= "name";
	private static final String	HEAD_PATH	= "state";

	private JPanel panel = null;
	
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
		JTable table = createCenter();
		
		createToolBar(table);
	}
	
	private void createToolBar(final JTable table)
	{
		JToolBar toolBar = new JToolBar();
		
		JButton reloadBut = new JButton("Reload");
		JButton startBut = new JButton("Start");
		JButton stopBut = new JButton("Stop");
		JButton uninstallBut = new JButton("Uninstall");
		
		toolBar.add(reloadBut);
		toolBar.add(startBut);
		toolBar.add(stopBut);
		toolBar.add(uninstallBut);
		
		reloadBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				loadOsgiInfo(table);
			}
		});
		
		panel.add(toolBar, BorderLayout.NORTH);
	}

	private JTable createCenter()
	{
		JTable osgiTable = new JTable();
		osgiTable.setAutoCreateRowSorter(true);
		setTableHeader(osgiTable, HEAD_PATH, HEAD_SIZE, HEAD_LEVEL);
		
		JScrollPane osgiPane = new JScrollPane(osgiTable);
		
		panel.add(osgiPane, BorderLayout.CENTER);
		
		return osgiTable;
	}

	private void loadOsgiInfo(JTable osgiTable)
	{
		loadOsgiInfo(osgiTable, true);
	}
	
	private void loadOsgiInfo(JTable osgiTable, boolean reload)
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
		
		ClientProxyFactoryBean factory =
				getClientProxy("http://10.0.31.249:6789/greeter", BundleServer.class);
		
		BundleServer server = (BundleServer) factory.create();
		
		List<SuRenBundle> bundles = server.getAll();
		for(SuRenBundle bundle : bundles)
		{
			Vector<Object> item = new Vector<Object>();
			
			item.add(bundle.getId());
			item.add(bundle.getName());
			item.add(bundle.getState());
			
			fillTable(osgiTable, item);
		}
	}
	
	private ClientProxyFactoryBean getClientProxy(String url, Class<BundleServer> clazz)
	{
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		
		factory = getClientProxy(url, clazz);
		
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
}