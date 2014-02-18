package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.log.JTextAreaAppender;
import org.suren.littlebird.server.MouseServer;
import org.suren.littlebird.server.SimpleServer;

@Menu(displayName = "MouseServer", parentMenu = ServerMenu.class, index = 0)
public class MouseServerMenuItem extends ArchMenu
{
	private JPanel panel = null;
	private JTextField portField = null;
	private JButton startBut = null;
	private JButton stopBut = null;
	private SimpleServer mouseServer = null;
	
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
				main.getContentPanel().add("MouseServer", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "MouseServer");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
		BorderLayout layout = new BorderLayout();
		panel.setLayout(layout);
		
		JPanel controlPanel = createControlPanel();
		JPanel logPanel = createLogPanel();
		
		panel.add(controlPanel, BorderLayout.NORTH);
		panel.add(logPanel, BorderLayout.CENTER);
		
		logger.debug("MouseServerPanel inited.");
	}

	private JPanel createLogPanel()
	{
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		
		JTextArea logTextArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(logTextArea);
		logPanel.add(scrollPane, BorderLayout.CENTER);
		
		JTextAreaAppender appender = new JTextAreaAppender();
		appender.addFilter(MouseServer.class.getName());
		appender.addFilter(this.getClass().getName());
		appender.setTargetArea(logTextArea);
		
		logger.putAppender(appender);
		
		return logPanel;
	}

	private JPanel createControlPanel()
	{
		JPanel controlPanel = new JPanel();
		portField = new JTextField("8989");
		startBut = new JButton("start");
		stopBut = new JButton("stop");
		
		controlPanel.add(portField);
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
				
				mouseServer = new MouseServer();
				if(mouseServer.init(port))
				{
					new Thread(mouseServer).start();
					
					logger.info("prepare to start mouse server.");
				}
				
				controlStatusCheck();
			}
		});
		stopBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(mouseServer == null)
				{
					return;
				}
				
				if(mouseServer.stop())
				{
					logger.info("mouseServer stoped.");
					
					mouseServer = null;
				}
				
				controlStatusCheck();
			}
		});
		
		controlStatusCheck();
		
		return controlPanel;
	}

	private void controlStatusCheck()
	{
		boolean running = false;
		
		if(mouseServer != null)
		{
			running = true;
		}
		
		portField.setEnabled(!running);
		startBut.setEnabled(!running);
		stopBut.setEnabled(running);
	}
}
