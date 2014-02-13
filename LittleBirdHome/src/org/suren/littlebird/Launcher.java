package org.suren.littlebird;

import java.util.Arrays;

import javax.swing.UnsupportedLookAndFeelException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.suren.littlebird.annotation.Menu;
import org.suren.littlebird.annotation.Publish;
import org.suren.littlebird.gui.MainFrame;

public class Launcher
{

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args)
	{
		Launcher launcher = new Launcher();
		launcher.init();
		
		MainFrame main = MainFrame.getInstance();
		
		main.setVisible(true);
		
		launcher.match();
	}
	
	private void match()
	{
		System.out.println(System.getProperty("os.arch"));
	}

	private void faceDetect()
	{
		System.load(Core.NATIVE_LIBRARY_NAME);
		
		String srcFile = "d:/12.jpg";
		
		Mat src = Highgui.imread(srcFile);
		Mat dst = Highgui.imread(srcFile);
		
		CascadeClassifier faceDetector =
				new CascadeClassifier("D:/Work/opencv/sources/data/haarcascades/" +
						"haarcascade_frontalface_alt2.xml");
		MatOfRect detectRect = new MatOfRect();
		
		faceDetector.detectMultiScale(src, detectRect);
		
		System.out.println(String.format("Detectd %s faces", Arrays.toString(detectRect.toArray())));
		
		for(Rect rect : detectRect.toArray())
		{
			Core.rectangle(dst,
					new Point(rect.x, rect.y),
					new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
			
			Mat subMat = src.submat(new Range(rect.y, rect.y + rect.height),
					new Range(rect.x, rect.x + rect.width));
			
			Highgui.imwrite("d:/" + System.currentTimeMillis() + ".jpg", subMat);
		}

		Highgui.imwrite("d:/2.jpg", dst);
	}

	private void init()
	{
		ResourceLoader loader = ResourceLoader.getInstance();
		
		loader.discover(Menu.class, Publish.class);
	}

}
