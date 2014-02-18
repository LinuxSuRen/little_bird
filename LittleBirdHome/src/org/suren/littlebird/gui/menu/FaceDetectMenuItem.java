package org.suren.littlebird.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Menu.Action;
import org.suren.littlebird.gui.MainFrame;

@Menu(displayName = "FaceDetect", parentMenu = ImageMenu.class, index = 0)
public class FaceDetectMenuItem extends ArchMenu
{
	private JPanel panel = null;
	private JToolBar toolBar;
	
	private JSplitPane splitPane = null;
	private JLabel leftLabel = null;
	private JLabel rightLabel = null;
	
	private JButton detectBut = null;
	
	private Map<JLabel, ImageIcon> orginImage = new HashMap<JLabel, ImageIcon>();
	private File detectPolicyFile = null;
	
	private JFileChooser fileChooser;
	
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
				panel.setLayout(new BorderLayout());
				main.getContentPanel().add("FaceDetect", panel);
				
				init();
			}
			
			main.getContentLayout().show(main.getContentPanel(), "FaceDetect");
			
			main.reDrawPanel();
		}
	};

	private void init()
	{
		if(splitPane != null)
		{
			return;
		}
		
		createToolBar();
		
		MainFrame mainFrame = MainFrame.getInstance();
		
		fileChooser = new JFileChooser();
		
		leftLabel = new JLabel();
		rightLabel = new JLabel();
		
		leftLabel.setHorizontalAlignment(JLabel.CENTER);
		leftLabel.setVerticalAlignment(JLabel.CENTER);
		rightLabel.setHorizontalAlignment(JLabel.CENTER);
		rightLabel.setVerticalAlignment(JLabel.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftLabel, rightLabel);
		splitPane.setDividerLocation((int)((mainFrame.getContentPanel().getWidth() - splitPane.getDividerSize()) * 0.5));
		
		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(splitPane, BorderLayout.CENTER);
		
		splitPane.addPropertyChangeListener(new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				String propertyName = evt.getPropertyName();
				
				if(propertyName.equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)
						|| propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY))
				{
					imageAdapt();
				}
			}
		});
		
		mainFrame.addComponentListener(new ComponentAdapter()
		{

			@Override
			public void componentResized(ComponentEvent e)
			{
				imageAdapt();
			}
		});
		
		leftLabel.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{
				int clickCount = e.getClickCount();
				Object srcObj = e.getSource();
				
				if(clickCount == 2 && srcObj instanceof JLabel)
				{
					JLabel label = (JLabel) srcObj;
					
					imageChoose(label);
				}
			}
		});
		
		leftLabel.setDropTarget(new DropTarget(){

			private static final long	serialVersionUID	= 1681721499777420711L;

			@Override
			public synchronized void drop(DropTargetDropEvent drop)
			{
				Transferable transferable = drop.getTransferable();
				drop.acceptDrop(DnDConstants.ACTION_REFERENCE);
				
				try
				{
					Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
					List<File> result = null;
					
					if(data == null || !(data instanceof List))
					{
						return;
					}
					
					result = (List<File>)data;
					if(result.size() > 0)
					{
						File file = result.get(0);
						
						imageChoose(leftLabel, file);
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
		});
		
		nativeLoad();
	}

	private void createToolBar()
	{
		toolBar = new JToolBar();
		
		JButton takePicBut = new JButton("Take");
		detectBut = new JButton("Detect");
		JButton resizeBut = new JButton("Resize");
		JButton selectXmlBut = new JButton("Policy");

		toolBar.add(takePicBut);
		toolBar.add(detectBut);
		toolBar.add(resizeBut);
		toolBar.add(selectXmlBut);
		
		takePicBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File file = new File(System.currentTimeMillis() + ".jpg");
				
				VideoCapture capture = new VideoCapture();
				if(!capture.open(0))
				{
					return;
				}
				
				Mat image = new Mat();
				capture.retrieve(image);
				Highgui.imwrite(file.getAbsolutePath(), image);
				capture.release();
				
				imageChoose(leftLabel, file);
				
				file.deleteOnExit();
			}
		});
		
		detectBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File file = faceDetect();
				if(file == null)
				{
					return;
				}
				
				imageChoose(rightLabel, file);
			}
		});
		
		resizeBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				splitPane.setDividerLocation(0.5);
			}
		});
		
		selectXmlBut.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				{
					return;
				}
				
				File file = fileChooser.getSelectedFile();
				if(file == null || file.isDirectory())
				{
					return;
				}
				
				detectPolicyFile = file;
			}
		});
	}
	
	private void nativeLoad()
	{
		String arch = System.getProperty("os.arch");
		if(arch.contains("64"))
		{
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME + "_64");
		}
		else
		{
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME + "_32");
		}
	}
	
	private File faceDetect()
	{
		ImageIcon icon = orginImage.get(leftLabel);
		File imageFile = null;
		if(icon == null || !(imageFile = new File(icon.getDescription())).isFile()
				|| detectPolicyFile == null
				|| !(detectPolicyFile.exists()))
		{
			return null;
		}
		
		Mat src = Highgui.imread(imageFile.getAbsolutePath());
		Mat dst = src.clone();
		
		CascadeClassifier faceDetector =
				new CascadeClassifier(detectPolicyFile.getAbsolutePath());
		MatOfRect detectRect = new MatOfRect();
		
		faceDetector.detectMultiScale(src, detectRect);
		
		for(Rect rect : detectRect.toArray())
		{
			Core.rectangle(dst,
					new org.opencv.core.Point(rect.x, rect.y),
					new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

		try
		{
			File file = new File("2.jpg");
			file.deleteOnExit();
			
			Highgui.imwrite(file.getAbsolutePath(), dst);
			
			return file;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	protected void imageAdapt()
	{
		imageAdapt(leftLabel);
		imageAdapt(rightLabel);
	}
	
	protected void imageAdapt(JLabel label)
	{
		ImageIcon icon = orginImage.get(label);
		if(icon == null)
		{
			return;
		}
		
		Point size = new Point(label.getWidth(), label.getHeight());
		adapt(new Point(icon.getIconWidth(), icon.getIconHeight()), size);
		
		if(size.x <= 0 || size.y <= 0)
		{
			return;
		}
		
		ImageIcon imageIcon = new ImageIcon();
		imageIcon.setImage(icon.getImage().getScaledInstance(size.x, size.y, Image.SCALE_FAST));
		
		label.setIcon(imageIcon);
	}

	protected void imageChoose(JLabel label)
	{
		imageChoose(label, null);
	}
	
	protected void imageChoose(JLabel label, File imageFile)
	{
		File file = null;
		if(label == null)
		{
			return;
		}
		
		if(imageFile == null)
		{
			if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			{
				return;
			}
			
			file = fileChooser.getSelectedFile();
			if(file == null || file.isDirectory())
			{
				return;
			}
		}
		else
		{
			file = new File(imageFile.getAbsolutePath());
		}
		
		ImageIcon icon = new ImageIcon(file.getAbsolutePath());
		
		orginImage.put(label, icon);
		
		ImageIcon imageIcon = new ImageIcon();
		
		Point size = new Point(label.getWidth(), label.getHeight());
		adapt(new Point(icon.getIconWidth(), icon.getIconHeight()), size);
		
		imageIcon.setImage(icon.getImage().getScaledInstance(size.x, size.y, Image.SCALE_DEFAULT));
		
		label.setIcon(imageIcon);
	}
	
	private void adapt(Point src, Point dst)
	{
		Point target = new Point(dst);
		
		if(src.getX() > target.getX())
		{
			dst.setLocation(target.getX(), dst.getX() / src.getX() * src.getY());
		}
		
		if(src.getY() > target.getY())
		{
			dst.setLocation(dst.getY() / src.getY() * src.getX(), dst.getY());
		}
		
		if(dst.getY() > target.getY())
		{
			dst.setLocation(target.getY() / dst.getY() * dst.getX(), target.getY());
		}
	}
}
