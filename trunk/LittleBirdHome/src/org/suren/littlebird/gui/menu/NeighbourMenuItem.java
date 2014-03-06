package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;
import org.suren.littlebird.gui.SuRenTextField;
import org.suren.littlebird.net.NetworkInfo;
import org.suren.littlebird.setting.NeighbourSetting;
import org.suren.littlebird.setting.SettingUtil;

@Menu(displayName = "Neighbour", parentMenu = NetworkMenu.class, index = 1,
	keyCode = KeyEvent.VK_N, modifiers = KeyEvent.CTRL_DOWN_MASK)
public class NeighbourMenuItem extends ArchMenu<NeighbourSetting>
{
	protected JPanel panel;
	private JToolBar controlbar;
	
	private NetworkGrid networkGrid;

	private final String	NEIGHBOUR_CFG_PATH	= "neighbour_cfg.xml";
	
	private ArrayBlockingQueue<NetworkInfo> networkInfo = new ArrayBlockingQueue<NetworkInfo>(10);
	private AtomicBoolean running = new AtomicBoolean(false);
	protected Object Reacher = new Object();
	
	private JTextField timeoutText;
	
	private String fromAddr;
	
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
				main.getContentPanel().add("Neighbour", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "Neighbour");
			
			main.reDrawPanel();
		}
	};

	protected void init()
	{
		if(controlbar != null)
		{
			return;
		}
		
		createControlBar();
		
		createCenterPanel();
	}

	private void createControlBar()
	{
		controlbar = new JToolBar("Control");
		
		JButton startBut = new JButton("Start");
		JButton stopBut = new JButton("Stop");
		final SuRenTextField aField = new SuRenTextField();
		final SuRenTextField bField = new SuRenTextField();
		final SuRenTextField cField = new SuRenTextField();
		final SuRenTextField dField = new SuRenTextField();
		final SuRenTextField eField = new SuRenTextField();
		final SuRenTextField fField = new SuRenTextField();
		final SuRenTextField gField = new SuRenTextField();
		final SuRenTextField hField = new SuRenTextField();
		JLabel timeLabel = new JLabel("Timeout:");
		timeoutText = new JTextField("3000");
		
		startBut.setMnemonic('s');
		stopBut.setMnemonic('o');
		
		aField.addComboText(eField);
		bField.addComboText(fField);
		cField.addComboText(gField);
		dField.addComboText(hField);
		
		NeighbourSetting neighbour = loadCfg();
		if(neighbour != null)
		{
			String fromHost = neighbour.getFromHost();
			if(fromHost != null)
			{
				String[] data = fromHost.split("\\.");
				if(data.length == 4)
				{
					aField.setText(data[0]);
					bField.setText(data[1]);
					cField.setText(data[2]);
					dField.setText(data[3]);
				}
			}
			
			String endHost = neighbour.getEndHost();
			if(endHost != null)
			{
				String[] data = endHost.split("\\.");
				if(data.length == 4)
				{
					eField.setText(data[0]);
					fField.setText(data[1]);
					gField.setText(data[2]);
					hField.setText(data[3]);
				}
			}
			
			timeoutText.setText(String.valueOf(neighbour.getTimeout()));
		}
		
		controlbar.add(startBut);
		controlbar.add(stopBut);
		controlbar.addSeparator();
		controlbar.add(aField);
		controlbar.add(new JLabel("."));
		controlbar.add(bField);
		controlbar.add(new JLabel("."));
		controlbar.add(cField);
		controlbar.add(new JLabel("."));
		controlbar.add(dField);
		controlbar.addSeparator();
		controlbar.add(eField);
		controlbar.add(new JLabel("."));
		controlbar.add(fField);
		controlbar.add(new JLabel("."));
		controlbar.add(gField);
		controlbar.add(new JLabel("."));
		controlbar.add(hField);
		controlbar.addSeparator();
		controlbar.add(timeLabel);
		controlbar.add(timeoutText);
		
		startBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				fromAddr = aField.getText() + "." + bField.getText() + "." + cField.getText() + ".";
				
				Object source = e.getSource();
				if(source instanceof Component)
				{
					pingRun.setTrigger((Component) source);
					
					new Thread(pingRun, "pingRun").start();
					
					NeighbourSetting neighbour = loadCfg(true);
					neighbour.setFromHost(fromAddr + dField.getText());
					neighbour.setEndHost(eField.getText() + "." + fField.getText()
							+ "." + gField.getText() + "." + hField.getText());
					
					int timeout = neighbour.getTimeout();
					try
					{
						timeout = Integer.parseInt(timeoutText.getText());
					}
					catch(NumberFormatException e1)
					{
					}
					
					neighbour.setTimeout(timeout);
					saveCfg(neighbour);
				}
			}
		});
		
		stopBut.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				synchronized (Reacher)
				{
					running.set(false);
					
					Reacher.notifyAll();
				}
			}
		});
		
		panel.add(controlbar, BorderLayout.NORTH);
	}

	private Component createNetGrid()
	{
		networkGrid = new NetworkGrid();
		GridLayout layout = new GridLayout(10, 25);
		layout.setHgap(5);
		layout.setVgap(5);
		networkGrid.setLayout(layout);
		
		return networkGrid;
	}

	private void createCenterPanel()
	{
		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(new CardLayout());
		
		cardPanel.add("NetGrid", createNetGrid());
		
		panel.add(cardPanel, BorderLayout.CENTER);
	}
	
	private TriggerRunnable pingRun = new TriggerRunnable()
	{
		private Component trigger;
		
		@Override
		public void run()
		{
			try
			{
				turnOff();

				new Thread(netGridRun, "netGridRun").start();
				int timeout = 1000;
				
				try
				{
					timeout = Integer.parseInt(timeoutText.getText());
				}
				catch(NumberFormatException e)
				{
				}
				
				int alive = 0;
				int dead = 0;
				for(int i = 1; i < 255; i++)
				{
					String host = fromAddr + i;
					
					InetAddress addr = InetAddress.getByName(host);
					
					long begin = System.currentTimeMillis();
					
					NetworkInfo info = new NetworkInfo();
					info.setHost(host);
					info.setReachable(addr.isReachable(timeout));
					info.setTime(System.currentTimeMillis() - begin);
					
					if(info.isReachable())
					{
						alive++;
					}
					else
					{
						dead++;
					}
					
					setTitleLog(alive, dead);
					
					networkInfo.put(info);
					
					System.out.println(host);
					
					synchronized (Reacher)
					{
						Reacher.notifyAll();
					}
					
					if(!running.get())
					{
						break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				turnOn();
			}
		}

		@Override
		public void setTrigger(Component trigger)
		{
			this.trigger = trigger;
		}

		@Override
		public void turnOff()
		{
			if(trigger != null)
			{
				trigger.setEnabled(false);
			}
			
			running.set(true);
			
			timeoutText.setEnabled(false);
		}

		@Override
		public void turnOn()
		{
			if(trigger != null)
			{
				trigger.setEnabled(true);
			}
			
			running.set(false);
			
			timeoutText.setEnabled(true);
		}
	};
	
	Runnable netGridRun = new Runnable()
	{
		
		@Override
		public void run()
		{
			networkGrid.clear();
			
			while(running.get())
			{
				synchronized (Reacher )
				{
					try
					{
						Reacher.wait(3000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						
						break;
					}
					
					NetworkInfo info = networkInfo.poll();
					if(info == null)
					{
						continue;
					}
					
					networkGrid.add(info);
				}
			}
		}
	};
	
	private void setTitleLog(int alive, int dead)
	{
		MainFrame main = MainFrame.getInstance();
		
		main.setTitle("alive : ", alive, "; dead : ", dead);
	}
	
	@Override
	protected boolean saveCfg(NeighbourSetting cfgObj)
	{
		SettingUtil<NeighbourSetting> setting = new SettingUtil<NeighbourSetting>();
		
		return setting.save(cfgObj, NEIGHBOUR_CFG_PATH, NeighbourSetting.class);
	}

	private NeighbourSetting loadCfg(boolean create)
	{
		NeighbourSetting neighbour = loadCfg();
		
		if(neighbour == null)
		{
			neighbour = new NeighbourSetting();
		}
		
		return neighbour;
	}

	@Override
	protected NeighbourSetting loadCfg()
	{
		SettingUtil<NeighbourSetting> setting = new SettingUtil<NeighbourSetting>();
		
		return setting.load(NEIGHBOUR_CFG_PATH, NeighbourSetting.class);
	}
	
	interface TriggerRunnable extends Runnable
	{
		public void setTrigger(Component trigger);
		
		public void turnOff();
		
		public void turnOn();
	}
	
	interface NetPanel
	{
		public void add(NetworkInfo info);
		
		public void del(NetworkInfo info);
		
		public void clear();
	}
	
	class NetworkGrid extends JPanel implements NetPanel
	{

		private static final long	serialVersionUID	= 1L;

		@Override
		public void add(NetworkInfo info)
		{
			JPanel panel = new JPanel();
			panel.setBackground(info.isReachable() ? Color.GREEN : Color.RED);
			panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
			panel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
			panel.setLayout(new BorderLayout());
			
			JLabel label = new JLabel(String.valueOf(info.getTime()), JLabel.CENTER);
			label.setToolTipText(info.getHost());
			panel.add(label, BorderLayout.CENTER);
			
			label.addMouseListener(new MouseAdapter()
			{
				private String sourceText;

				@Override
				public void mousePressed(MouseEvent e)
				{
					JLabel label = getSource(e);
					sourceText = label.getText();
					label.setText(label.getToolTipText());
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					JLabel label = getSource(e);
					label.setText(sourceText);
				}
				
				private JLabel getSource(MouseEvent e)
				{
					Object source = e.getSource();
					if(source instanceof JLabel)
					{
						return (JLabel) source;
					}
					else
					{
						return null;
					}
				}
			});
			
			add(panel);
			
			MainFrame main = MainFrame.getInstance();
			main.reDrawPanel();
		}

		@Override
		public void del(NetworkInfo info)
		{
		}

		@Override
		public void clear()
		{
			removeAll();
		}
	}
}
