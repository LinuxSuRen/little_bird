package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.suren.littlebird.ArchServerListener;
import org.suren.littlebird.ClientInfo;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.log.JTextAreaAppender;
import org.suren.littlebird.server.MonitorServer;
import org.suren.littlebird.server.SimpleServer;

@Menu(displayName = "ScreenServer", parentMenu = ServerMenu.class, index = 1)
public class ScreenServerMenuItem extends ArchMenu<Object>
{
	private JPanel panel = null;
	private JTextField portField = null;
	private JTextField qualityField = null;
	private JButton startBut = null;
	private JButton stopBut = null;
	private DefaultListModel clientListModel = null;
	private JList clientList = null;
	private SimpleServer monitorServer = null;
	
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
				panel.setBackground(Color.BLACK);
				main.getContentPanel().add("ScreenServer", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "ScreenServer");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
		BorderLayout layout = new BorderLayout();
		panel.setLayout(layout);
		
		JPanel controlPanel = createControlPanel();
		JPanel logPanel = createLogPanel();
		
		JPanel clientListPanel = createClientPanel();
		
		panel.add(controlPanel, BorderLayout.NORTH);
		panel.add(logPanel, BorderLayout.CENTER);
		panel.add(clientListPanel, BorderLayout.EAST);
	}

	private JPanel createLogPanel()
	{
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		
		JTextArea logTextArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(logTextArea);
		logPanel.add(scrollPane, BorderLayout.CENTER);
		
		JTextAreaAppender appender = new JTextAreaAppender();
		appender.setRowsLimit(100);
		appender.addFilter(this.getClass().getName());
		appender.addFilter(MonitorServer.class.getName());
		appender.setTargetArea(logTextArea);
		
		logger.putAppender(appender);
		
		return logPanel;
	}

	private JPanel createControlPanel()
	{
		JPanel controlPanel = new JPanel();
		portField = new JTextField("8990");
		qualityField = new JTextField("0.1");
		startBut = new JButton("start");
		stopBut = new JButton("stop");
		
		controlPanel.add(portField);
		controlPanel.add(qualityField);
		controlPanel.add(startBut);
		controlPanel.add(stopBut);
		
		startBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent event)
			{
				String strPort = portField.getText();
				int port = -1;
				
				try
				{
					port = Integer.parseInt(strPort);
				}
				catch(NumberFormatException e)
				{
					logger.error("the port is not a number : " + strPort);
					
					return;
				}
				
				monitorServer = new MonitorServer();
				if(monitorServer.init(port))
				{
					qualityChange();
					
					new Thread(monitorServer).start();
					
					logger.info("prepare to start mouse server.");
					
					monitorServer.setListener(serverListener);
				}
				
				controlStatusCheck();
			}
		});
		stopBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("monitorServer:"+monitorServer);
				if(monitorServer == null)
				{
					return;
				}
				
				if(monitorServer.stop())
				{
					logger.info("monitor server stoped.");
					
					monitorServer = null;
				}
				
				controlStatusCheck();
			}
		});
		qualityField.addFocusListener(new FocusListener()
		{
			
			@Override
			public void focusLost(FocusEvent e)
			{
				qualityChange();
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{
			}
		});
		
		controlStatusCheck();
		
		return controlPanel;
	}
	
	private ArchServerListener serverListener = new ArchServerListener()
	{
		
		@Override
		public void onLine(ClientInfo clientInfo)
		{
			if(clientInfo == null)
			{
				return;
			}
			
			clientListModel.addElement(clientInfo.getAddress());
		}
		
		@Override
		public void offLine(ClientInfo clientInfo)
		{
		}
	};

	private JPanel createClientPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		clientListModel = new DefaultListModel();
		clientList = new JList(clientListModel);
		
		panel.add(clientList, BorderLayout.CENTER);
		
		return panel;
	}
	
	private void qualityChange()
	{
		if(monitorServer instanceof MonitorServer)
		{
			String quality = qualityField.getText();
			
			try
			{
				((MonitorServer)monitorServer).setQuality(Float.parseFloat(quality));
			}
			catch(NumberFormatException e)
			{
			}
		}
	}

	private void controlStatusCheck()
	{
		boolean running = false;
		
		if(monitorServer != null)
		{
			running = true;
		}
		
		portField.setEnabled(!running);
		startBut.setEnabled(!running);
		stopBut.setEnabled(running);
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
