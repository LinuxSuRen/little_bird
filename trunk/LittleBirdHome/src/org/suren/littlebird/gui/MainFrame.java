package org.suren.littlebird.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.suren.littlebird.Launcher;
import org.suren.littlebird.ResourceLoader;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.menu.ArchMenu;
import org.suren.littlebird.log.ArchLogger;
import org.suren.littlebird.log.ConsoleAppender;

public class MainFrame extends JFrame
{
	private static final long	serialVersionUID	= 1277152977364088514L;
	private static MainFrame mainFrame;
	private AtomicReference<JPanel> contentPanel = new AtomicReference<JPanel>(null);
	private AtomicReference<CardLayout> contentLayout =
			new AtomicReference<CardLayout>(new CardLayout());
	private ArchLogger logger = ArchLogger.getInstance();
	private ConsoleAppender appender = new ConsoleAppender();
	private String title;

	private MainFrame()
	{
		mainFrame = this;
		
		JPanel panel = new JPanel();
		panel.setLayout(contentLayout.get());
		contentPanel.set(panel);
		
		this.add(panel);
		title = "LittleBird";
		setTitle("");
		
		lazyMenu();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		URL iconRes = Launcher.class.getResource("/arch.jpg");
		this.setIconImage(new ImageIcon(iconRes).getImage());
		this.setSize(screenSize.width / 2, screenSize.height / 2);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationByPlatform(true);
	}
	
	public static MainFrame getInstance()
	{
		if(mainFrame == null)
		{
			mainFrame = new MainFrame();
		}
		
		return mainFrame;
	}
	
	public JPanel getContentPanel()
	{
		return contentPanel.get();
	}
	
	public CardLayout getContentLayout()
	{
		return contentLayout.get();
	}
	
	private void lazyMenu()
	{
		logger.putAppender(appender);
		appender.addFilter(this.getClass().getName());
		
		SwingUtilities.invokeLater(new Thread("lazyMenuThread")
		{
			public void run()
			{
				ResourceLoader loader = ResourceLoader.getInstance();
				appender.addFilter(this.getClass().getName());
				
				while(!loader.isFinished())
				{
				}
				
				List<Class<?>> result = loader.getResult(Menu.class);
				List<Class<?>> tmpResult = new ArrayList<Class<?>>();
				if(result == null || result.size() == 0)
				{
					logger.warn("no menus found.");
					
					return;
				}
				
				tmpResult.addAll(result);
				
				JMenuBar menuBar = new JMenuBar();
				Map<Class<?>, JMenuItem> menuMap = new HashMap<Class<?>, JMenuItem>();
				
				for(int i = 0; i < result.size();)
				{
					if(menuMap.get(result.get(i)) != null)
					{
						continue;
					}
					
					try
					{
						Object obj = result.get(i).newInstance();
						if(!(obj instanceof ArchMenu))
						{
							continue;
						}
						
						Menu menuAnno = result.get(i).getAnnotation(Menu.class);

						if(menuAnno.parentMenu().equals(Object.class))
						{
							JMenu menu = new JMenu(menuAnno.displayName());
							
							menuBar.add(menu);
							menuMap.put(result.get(i), menu);
							result.remove(result.get(i));
							
							i = 0;
						}
						else
						{
							buildParent(tmpResult, result, menuAnno.parentMenu(),
									menuMap, result.get(i), menuBar);
							
							i = 0;
						}
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
				
				reOrder(menuMap, menuBar);
				
				mainFrame.setJMenuBar(menuBar);
				reDrawFrame();
				
				logger.info("menu loaded.");
			}
		});
		
		logger.info("lazy load menu.");
	}
	
	private void reOrder(Map<Class<?>, JMenuItem> menuMap, Container container)
	{
		Set<Class<?>> keys = menuMap.keySet();
		TreeMap<Integer, Component> map = new TreeMap<Integer, Component>();
		
		Component[] components = null;
		if(container instanceof JMenu)
		{
			components = ((JMenu)container).getMenuComponents();
		}
		else if(container instanceof JMenuBar || container instanceof JMenuItem)
		{
			components = container.getComponents();
		}
		
		if(components == null)
		{
			return;
		}
		
		for(Component obj : components)
		{
			for(Class<?> key : keys)
			{
				Menu menu = key.getAnnotation(Menu.class);
				int index = menu.index();
				
				if(obj == menuMap.get(key))
				{
					container.remove(obj);
					map.put(index, obj);
					
					if(obj instanceof Container)
					{
						reOrder(menuMap, (Container) obj);
					}
					
					break;
				}
			}
		}
		
		Set<Integer> treeKeys = map.keySet();
		for(int key : treeKeys)
		{
			container.add(map.get(key));
		}
	}
	
	private boolean buildParent(List<Class<?>> allResult, List<Class<?>> result,
			Class<?> parentCls, Map<Class<?>, JMenuItem> menuMap,
			Class<?> subCls, JMenuBar menuBar)
	{
		Menu menuAnno = parentCls.getAnnotation(Menu.class);
		Menu subAnno = subCls.getAnnotation(Menu.class);
		if(menuAnno == null || subAnno == null)
		{
			return false;
		}
		
		boolean found = false;
		for(Class<?> res : allResult)
		{
			if(res.equals(parentCls))
			{
				found = true;
				break;
			}
		}
		
		if(found)
		{
			JMenuItem menu = menuMap.get(parentCls);
			if(menu == null)
			{
				if(menuAnno.parentMenu().equals(Object.class))
				{
					JMenu rootMenu = new JMenu(menuAnno.displayName());
					
					menuBar.add(rootMenu);
					menuMap.put(parentCls, rootMenu);
					result.remove(parentCls);
					
					return true;
				}
				else
				{
					return buildParent(allResult, result, menuAnno.parentMenu(),
							menuMap, parentCls, menuBar);
				}
			}
			else
			{
				JMenuItem menuItem = new JMenuItem(subAnno.displayName());
				
				menu.add(menuItem);
				menuMap.put(subCls, menuItem);
				result.remove(subCls);
				menu.repaint();
				
				try
				{
					Object obj = subCls.newInstance();
					if(!(obj instanceof ArchMenu))
					{
						return false;
					}
					
					ArchMenu archMenu = (ArchMenu)obj;
					Field actionField = null;
					for(Field field : subCls.getDeclaredFields())
					{
						Action actionAnno = field.getAnnotation(Action.class);
						if(actionAnno != null)
						{
							actionField = field;
							break;
						}
					}
					if(actionField != null)
					{
						actionField.setAccessible(true);
						
						Object fieldObj = actionField.get(archMenu);
						if(fieldObj instanceof ActionListener)
						{
							menuItem.addActionListener((ActionListener)fieldObj);
						}
					}
				}
				catch (InstantiationException e1)
				{
					e1.printStackTrace();
				}
				catch (IllegalAccessException e1)
				{
					e1.printStackTrace();
				}
				
				return true;
			}
		}
		
		return false;
	}

	public void reDrawPanel()
	{
		contentPanel.get().validate();
		contentPanel.get().repaint();
	}
	
	public void reDrawFrame()
	{
		mainFrame.validate();
		mainFrame.repaint();
	}

	@Override
	public void setTitle(String title)
	{
		if(title == null || "".equals(title))
		{
			super.setTitle(this.title);
		}
		else
		{
			super.setTitle(this.title + " -- " + title);
		}
	}
	
	public void setTitle(Object ... titles)
	{
		if(titles == null)
		{
			return;
		}
		
		StringBuffer buffer = new StringBuffer();
		for(Object title : titles)
		{
			buffer.append(title);
		}
		
		setTitle(buffer.toString());
	}
}
