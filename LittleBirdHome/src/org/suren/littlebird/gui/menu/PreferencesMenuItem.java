package org.suren.littlebird.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;

@Menu(displayName = "Preferences", parentMenu = WindowMenu.class, index = 0)
public class PreferencesMenuItem extends ArchMenu
{
	private JPanel panel;
	private String name;
	
	@Action
	private ActionListener action = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			MainFrame frame = MainFrame.getInstance();
			if(panel == null)
			{
				init();
			}
			
			frame.getContentLayout().show(frame.getContentPanel(), name);
			
			logger.debug("Preferences Show.");
		}
	};
	
	private void init()
	{
		name = "Preferences";
		panel = new JPanel();
		MainFrame.getInstance().getContentPanel().add(name, panel);
		
		List<LookAndFeelInfo> feels = getLookAndFeel();
		JButton applyBut = new JButton("apply");
		JLabel feelLabel = new JLabel("LookAndFeel");
		final JList fellList = new JList();
		
		panel.add(applyBut);
		panel.add(feelLabel);
		panel.add(fellList);
		
		DefaultListModel listModel = new DefaultListModel();
		for(LookAndFeelInfo feel : feels)
		{
		    listModel.addElement(feel.getClassName());
		}
		fellList.setModel(listModel);
		fellList.setSelectedIndex(0);
		
		applyBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object obj = fellList.getSelectedValue();
				if(obj instanceof String)
				{
					lookFeelTurn(obj.toString());
				}
			}
		});
		
		MainFrame.getInstance().reDrawPanel();
		logger.info("Preferences Panel inited.");
	}
	
	private List<LookAndFeelInfo> getLookAndFeel()
	{
		List<LookAndFeelInfo> feelList = new ArrayList<LookAndFeelInfo>();
		
		LookAndFeelInfo[] installedLooks = UIManager.getInstalledLookAndFeels();
		if(installedLooks != null)
		{
			for(LookAndFeelInfo installed : installedLooks)
			{
				feelList.add(installed);
			}
		}
		
		return feelList;
	}
	
	private void lookFeelTurn(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(name);
					SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
					MainFrame.getInstance().validate();
				}
				catch (UnsupportedLookAndFeelException e)
				{
					e.printStackTrace();
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
