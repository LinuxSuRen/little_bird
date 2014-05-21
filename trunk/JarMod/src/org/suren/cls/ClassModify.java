package org.suren.cls;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.suren.jar.JarUpdater;

/**
 * @author suren
 *
 */
public class ClassModify
{
	private ClassPool		pool;
	private List<String>	packageList;

	private void init()
	{
		pool = ClassPool.getDefault();
	}
	
	public boolean modify(String classPath, String mainClsName, String outDir) throws Exception
	{
		if(classPath == null)
		{
			return false;
		}
		
		if(mainClsName == null && (mainClsName = tryFindMainClass(classPath)) == null)
		{
			return false;
		}
		
		init();
		pool.insertClassPath(classPath);
		
		CtClass stringArrayCls = pool.get(String[].class.getName());
		
		if(!createClientCls(outDir))
		{
			return false;
		}
		
		CtClass mainCls = null;
		try
		{
			addPackage(mainClsName);
			
			mainCls = pool.get(mainClsName);
			
			CtMethod prepareMethod = new CtMethod(
					CtClass.voidType,
					"prepare",
					new CtClass[]{stringArrayCls},
					mainCls);
			
			prepareMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
			prepareMethod.setBody("{}");
			mainCls.addMethod(prepareMethod);
			
			StringBuilder bodyBuf = new StringBuilder();
			bodyBuf.append("{\n");
			bodyBuf.append("try{");
			bodyBuf.append("int len = $1.length;\n\n");
			bodyBuf.append("String servU = $1[(len - 5)];\n");
			bodyBuf.append("String servP = $1[(len - 4)];\n");
			bodyBuf.append("String targU = $1[(len - 3)];\n");
			bodyBuf.append("String kvmBg = $1[(len - 2)];\n");
			bodyBuf.append("String clientUrl = $1[(len - 1)];\n");
			bodyBuf.append("new com.ami.kvm.jviewer.Client().addRules(servU, servP, clientUrl, targU, kvmBg);\n");
			bodyBuf.append("}catch(Exception e){e.printStackTrace();new com.ami.kvm.jviewer.Client().clear();}\n");
			bodyBuf.append("finally{Runtime.getRuntime().addShutdownHook(new com.ami.kvm.jviewer.ClientCloseThread());}");
			bodyBuf.append("}\n");
			
			prepareMethod.setBody(bodyBuf.toString());
			
			CtMethod mainMethod = mainCls.getDeclaredMethod("main", new CtClass[]{
					pool.get(String[].class.getName())
			});
			mainMethod.insertBefore("try{prepare($1);}catch(Exception e){e.printStackTrace();}\n");
			
			mainCls.writeFile(outDir);
			
			System.out.println("class modify done.");
			
			return true;
		}
		catch(NotFoundException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	private String tryFindMainClass(String classPath)
	{
		if(classPath == null)
		{
			return null;
		}
		
		JarFile jarFile = null;
		try
		{
			jarFile = new JarFile(classPath);
			
			Manifest manifest = jarFile.getManifest();
			Attributes attr = null;
			
			if(manifest == null || (attr = manifest.getMainAttributes()) == null)
			{
				return null;
			}
			
			System.out.println("try find main class.");
			
			return attr.getValue("Main-Class");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(jarFile != null)
			{
				try
				{
					jarFile.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	private CtClass createCls(Class<?> srcCls, CtClass superCls, String targetCls, String outDir)
	{
		CtClass cls = null;
		
		try
		{
			cls = pool.makeClass(targetCls, superCls);
			cls.setModifiers(Modifier.PUBLIC);
			
			String innerClientCls = srcCls.getName();
			CtClass clientCls = pool.get(innerClientCls);
			copy(clientCls, cls);
			
			cls.writeFile(outDir);
		}
		catch (CannotCompileException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (NotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			return null;
		}
		
		return cls;
	}
	
	private boolean createClientCls(String outDir)
	{
		String clientClsName = "com.ami.kvm.jviewer.Client";
		if(createCls(Client.class, null, clientClsName, outDir) == null)
		{
			return false;
		}
		addPackage(clientClsName);
		
		String progressClsName = "com.ami.kvm.jviewer.Progress";
		if(createCls(Progress.class, null, progressClsName, outDir) == null)
		{
			return false;
		}
		addPackage(progressClsName);
		
		try
		{
			String clientThreadClsName = "com.ami.kvm.jviewer.ClientCloseThread";
			CtClass clientThreadcls = createCls(ClientCloseThread.class,
					pool.get("java.lang.Thread"),
					clientThreadClsName,
					outDir);
			if(clientThreadcls == null)
			{
				return false;
			}
			else
			{
				addPackage(clientThreadClsName);
			}

			String dialogCloserClsName = "com.ami.kvm.jviewer.DialogCloser";
			CtClass dialogCloserCls = createCls(DialogCloser.class,
					pool.get("java.awt.event.WindowAdapter"),
					dialogCloserClsName,
					outDir);
			if(dialogCloserCls == null)
			{
				return false;
			}
			else
			{
				addPackage(dialogCloserClsName);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	private boolean copy(CtClass src, CtClass target)
	{
		String srcName = src.getName();

		try
		{
			for(CtMethod method : src.getDeclaredMethods())
			{
				if(method.getDeclaringClass().getName().startsWith(srcName))
				{
					target.addMethod(CtNewMethod.copy(method, method.getName(),
							target, null));
				}
			}
			
			for(CtField field : src.getDeclaredFields())
			{
				target.addField(new CtField(field, target));
			}
		}
		catch (CannotCompileException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	static class Client
	{
		private static String	ip;
		private static String	clientIp;
		private static String	targetIp;
		private static String	kvmBridge;
		private static int		port;
		private static List<String> cmdList;
		
		private static Map<String, Object> progress;
		private static final String PRO_LEVEL_1 = "level_01";
		private static final String PRO_LEVEL_2 = "level_02";
		private static final String PRO_LEVEL_3 = "level_03";
		private static final String PRO_LEVEL_4 = "level_04";
		private static final String PRO_LEVEL_5 = "level_05";
		private static final String PRO_LEVEL_001 = "level_0001";

		private static final String dialogTitle = "kvm init";
		private static JDialog dialog;
		private static JProgressBar progressBar;
		
		private void log(CharSequence charSeq)
		{
			System.out.println(charSeq);
		}

		public void addRules(String servUrl, String servP, String clientUrl,
				String targetUrl, String kvmBg)
				throws UnknownHostException, IOException
		{
			ip = servUrl;
			port = Integer.parseInt(servP);
			clientIp = clientUrl;
			targetIp = targetUrl;
			kvmBridge = kvmBg;
			
			showLoadingDialog();
			
			progress = new HashMap<String, Object>();
			
			if(progress.size() == 0)
			{
				progress.put(PRO_LEVEL_1, createProgressObj(1, 10, "param init"));
				progress.put(PRO_LEVEL_2, createProgressObj(2, 30, "connect server"));
				progress.put(PRO_LEVEL_3, createProgressObj(3, 40, "cmd init"));
				progress.put(PRO_LEVEL_4, createProgressObj(4, 60, "send cmd"));
				progress.put(PRO_LEVEL_5, createProgressObj(5, 100, "connect over"));
				
				progress.put(PRO_LEVEL_001, createProgressObj(30, 100, "send cmd error"));
			}

			log("serverUrl : " + servUrl + "; serverPort : " + servP +
					"; targetUrl : " + targetUrl + "; kvmBridge : " + kvmBg);
			
			updateProgress(PRO_LEVEL_1);
			
			boolean sendResult = sendCmd(true);
			if(!sendResult)
			{
				updateProgress(PRO_LEVEL_001);
				
				clear();
				
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			dialog.setVisible(false);
		}
		
		private Object createProgressObj(int level, int value, String text)
		{
			String clsStr = "com.ami.kvm.jviewer.Progress";
			
			try
			{
				Class<?> cls = Class.forName(clsStr);
				
				Method setTextMethod = cls.getMethod("setText", String.class);
				Method setValueMethod = cls.getMethod("setValue", Integer.class);
				Method setLevelMethod = cls.getMethod("setLevel", Integer.class);
				
				Object obj = cls.newInstance();
				
				setTextMethod.invoke(obj, text);
				setValueMethod.invoke(obj, value);
				setLevelMethod.invoke(obj, level);
				
				return obj;
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (SecurityException e)
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
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
			
			return new Object();
		}
		
		public boolean sendCmd(boolean append)
		{
			log("ip : " + ip + "; port : " + port);
			
			Socket socket = new Socket();
			
			try
			{
				SocketAddress address = new InetSocketAddress(ip, port);
				String type = "delete";
				
				updateProgress(PRO_LEVEL_2);
				
				if(append)
				{
					type = "append";
				}
				
				for(int i = 0; i < 3; i++)
				{
					try
					{
						socket.setSoTimeout(10000);
						socket.connect(address, 10000);
						
						break;
					}
					catch (SocketTimeoutException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				if(!socket.isConnected())
				{
					return false;
				}
				
				updateProgress(PRO_LEVEL_3);
				
				cmdInit();
				
				OutputStream outStream = null;
				InputStream inStream = null;
				byte[] buffer = new byte[20480];
				
				try
				{
					outStream = socket.getOutputStream();
					inStream = socket.getInputStream();
					
					log("begin send cmd list. size : " + cmdList.size());
					
					updateProgress(PRO_LEVEL_4);
					
					for(String cmd : cmdList)
					{
						outStream.write(type.getBytes());
						if(inStream.read(buffer) <= 0)
						{
							log("send cmd type error.");
							return false;
						}
						
						int len = cmd.length();
						String strLen = String.valueOf(len);
						int size = 5 - strLen.length();
						for(int i = 0; i < size; i++)
						{
							strLen = "0" + strLen;
						}
						
						log("strLen : " + strLen);
						
						outStream.write((strLen + cmd).getBytes());
						if(inStream.read(buffer) <= 0)
						{
							log("send cmd error.");
							return false;
						}
						
						log("sended : " + cmd);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					
					return false;
				}
			}
			finally
			{
				if(socket != null)
				{
					try
					{
						socket.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				updateProgress(PRO_LEVEL_5);
			}
			
			return true;
		}
		
		private void cmdInit()
		{
			if(cmdList == null)
			{
				cmdList = new ArrayList<String>();
			}
			
			if(cmdList.size() > 0)
			{
				return;
			}
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5120 --jump DNAT --to-destination "
					+ targetIp + ":5120");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5122 --jump DNAT --to-destination "
					+ targetIp + ":5122");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5123 --jump DNAT --to-destination "
					+ targetIp + ":5123");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5900 --jump DNAT --to-destination "
					+ targetIp + ":5900");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5901 --jump DNAT --to-destination "
					+ targetIp + ":5901");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 7578 --jump DNAT --to-destination "
					+ targetIp + ":7578");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 623 --jump DNAT --to-destination "
					+ targetIp + ":623");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol udp --source "
					+ clientIp
					+ " --dport 255 --jump DNAT --to-destination "
					+ targetIp + ":255");
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5120 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5122 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5123 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5900 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 5901 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 7578 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ clientIp
					+ " --dport 623 --jump SNAT --to-source "
					+ kvmBridge);
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol udp --source "
					+ clientIp
					+ " --dport 255 --jump SNAT --to-source "
					+ kvmBridge);
			
			StringBuffer buffer = new StringBuffer();
			
			for(String cmd : cmdList)
			{
				buffer.append(cmd).append("\n");
			}
			
			if(buffer.length() > 0)
			{
				cmdList.clear();
				
				cmdList.add(buffer.substring(0, buffer.length() - 1));
			}
		}
		
		private void showLoadingDialog()
		{
			dialog = (dialog == null ? new JDialog() : dialog);
			progressBar = (progressBar == null ? new JProgressBar() : progressBar);
			
			progressBar.setOpaque(true);
			progressBar.setVisible(true);
			progressBar.setStringPainted(true);

			dialog.add(progressBar);
			
			dialog.setResizable(false);
			dialog.setSize(500, 150);
			dialog.setTitle(dialogTitle);
			dialog.setLocationByPlatform(true);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			try
			{
				Class<?> cls = Class.forName("com.ami.kvm.jviewer.DialogCloser");
				Object obj = cls.newInstance();
				
				if(obj instanceof WindowAdapter)
				{
					dialog.addWindowListener((WindowAdapter)obj);
				}
			}
			catch(ClassNotFoundException e)
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
		
		private void updateProgress(String key)
		{
			String clsStr = "com.ami.kvm.jviewer.Progress";
			
			Object value = progress.get(key);
			if(value == null || !value.getClass().getName().equals(clsStr))
			{
				return;
			}
			
			try
			{
				Class<?> cls = Class.forName(clsStr);
				
				Method getTextMethod = cls.getMethod("getText");
				Method getValueMethod = cls.getMethod("getValue");
				Method getLevelMethod = cls.getMethod("getLevel");
				
				Object textObj = getTextMethod.invoke(value);
				Object valObj = getValueMethod.invoke(value);
				Object levelObj = getLevelMethod.invoke(value);
				
				String text = textObj != null ? textObj.toString() : "";
				String valStr = valObj != null ? valObj.toString() : "";
				int val = 0;
				String level = levelObj != null ? levelObj.toString() : "0";
				
				try
				{
					val = Integer.parseInt(valStr);
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
				}
				
				progressBar.setString(text);
				progressBar.setValue(val);
				
				dialog.setTitle(dialogTitle + " - stage " + level);
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch(NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}

		public void clear()
		{
			sendCmd(false);
		}
	}
	
	static class Progress
	{
		private int level;
		private int value;
		private String text;
		public int getLevel()
		{
			return level;
		}
		public void setLevel(Integer level)
		{
			this.level = level;
		}
		public int getValue()
		{
			return value;
		}
		public void setValue(Integer value)
		{
			this.value = value;
		}
		public String getText()
		{
			return text;
		}
		public void setText(String text)
		{
			this.text = text;
		}
	}

	static class ClientThread extends Thread
	{
		private ArrayList<Integer> ids;
		private String host;
		private int port;
		
		public void run()
		{
			try
			{
				while (true)
				{
					try
					{
						Thread.sleep(10000L);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					Socket socket = new Socket();
					try
					{
						SocketAddress addr = new InetSocketAddress(host, port);
						socket.connect(addr);
						
						OutputStream os = socket.getOutputStream();
						InputStream is = socket.getInputStream();

						byte[] b = new byte[1024];

						for (Integer id : ids)
						{
							os.write("renewal".getBytes());

							int len = is.read(b);
							String str = new String(b, 0, len);
							if ("ok".equals(str))
							{
								os.write(id.toString().getBytes());
							}

							len = is.read(b);
							str = new String(b, 0, len);
							if (!"continue".equals(str))
							{
								continue;
							}
						}

						os.write("end".getBytes());
					}
					finally
					{
						if(socket != null)
						{
							socket.close();
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public ArrayList<Integer> getIds()
		{
			return ids;
		}

		public void setIds(ArrayList<Integer> ids)
		{
			this.ids = ids;
		}

		public String getHost()
		{
			return host;
		}

		public void setHost(String host)
		{
			this.host = host;
		}

		public int getPort()
		{
			return port;
		}

		public void setPort(int port)
		{
			this.port = port;
		}
	}
	
	static class ClientCloseThread extends Thread
	{
		public void run()
		{
			try
			{
				Class<?> cls = Class.forName("com.ami.kvm.jviewer.Client");
				
				Object instance = cls.newInstance();
				Method clearMethod = cls.getMethod("clear");
				
				clearMethod.invoke(instance);
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
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	static class DialogCloser extends WindowAdapter
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			if(permitForCloseExit())
			{
				clear();
				
				System.exit(0);
			}
		}

		private void clear()
		{
			try
			{
				Class<?> cls = Class.forName("com.ami.kvm.jviewer.Client");
				
				Object instance = cls.newInstance();
				Method clearMethod = cls.getMethod("clear");
				
				clearMethod.invoke(instance);
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
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}

		private boolean permitForCloseExit()
		{
			return false;
		}
	
	}
	
	static class ClassLoader extends URLClassLoader
	{

		public ClassLoader(URL[] urls)
		{
			super(urls);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException
		{
			Class<?> cls = null;
			
			try
			{
				cls = super.findClass(name);
			}
			catch(ClassNotFoundException e)
			{
				URL[] urls = this.getURLs();
				
				for(URL url : urls)
				{
					location(url, name);
				}
			}
			
			return cls;
		}

		private void location(URL url, String name) throws ClassNotFoundException
		{
			String path = name.replace(".", "/");
			
			try
			{
				ByteArrayOutputStream outStream = null;
				
				JarInputStream inStream = new JarInputStream(url.openStream());
				
				JarEntry entry = null;
				while((entry = inStream.getNextJarEntry()) != null)
				{
					if(entry.getName().equals(path))
					{
						outStream = new ByteArrayOutputStream();
						
						new JarUpdater().update(inStream, outStream);
						
						break;
					}
				}
				
				inStream.close();
				
				byte[] clsBuf = outStream.toByteArray();
				
				defineClass(name, clsBuf, 0, clsBuf.length);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ClassNotFoundException();
			}
		}
	}

	public List<String> getPackageList()
	{
		return packageList;
	}
	
	public void addPackage(String name)
	{
		if(packageList == null)
		{
			packageList = new ArrayList<String>();
		}
		
		packageList.add(name);
	}
}
