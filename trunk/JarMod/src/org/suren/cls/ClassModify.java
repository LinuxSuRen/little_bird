package org.suren.cls;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.codec.binary.Base64;
import org.suren.jar.JarUpdater;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

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
//			String mainClsName = "com.ami.kvm.jviewer.JViewer";
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
			bodyBuf.append("int len = $1.length;\n\n");
			bodyBuf.append("String servU = $1[0];\n");
			bodyBuf.append("String servP = $1[(len - 2)];\n");
			bodyBuf.append("String targU = $1[(len - 1)];\n");
			bodyBuf.append("try{");
			bodyBuf.append("new com.ami.kvm.jviewer.Client().addRules(servU, servP, targU);\n");
			bodyBuf.append("}catch(Exception e){e.printStackTrace();}\n");
			bodyBuf.append("}\n");
			
			prepareMethod.setBody(bodyBuf.toString());
			
			CtMethod mainMethod = mainCls.getDeclaredMethod("main", new CtClass[]{
					pool.get(String[].class.getName())
			});
			mainMethod.insertBefore("try{prepare($1);}catch(Exception e){}\n");
			mainMethod.insertAfter("{new com.ami.kvm.jviewer.Client().clear();\n}", true);
			
			mainCls.writeFile(outDir);
			
//			String winFrameClsName = "com.ami.kvm.jviewer.gui.WindowFrame";
//			addPackage(winFrameClsName);
//			CtClass winFrameCls = pool.get(winFrameClsName);
//			
//			CtConstructor winFrameconstructor = winFrameCls.getDeclaredConstructor(null);
//			if(winFrameconstructor != null)
//			{
//				StringBuilder buffer = new StringBuilder();
//				
//				buffer.append("addWindowListener(new com.ami.kvm.jviewer.gui.");
//				buffer.append(WindowCloseEvent.class.getSimpleName());
//				buffer.append("());\n");
//				
//				winFrameconstructor.insertAfter(buffer.toString());
//			}
//			
//			winFrameCls.writeFile(outDir);
		}
		catch(NotFoundException e)
		{
			e.printStackTrace();
		}
		
		return false;
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
		
		try
		{
			String clientThreadClsName = "com.ami.kvm.jviewer.ClientThread";
			CtClass cls = createCls(ClientThread.class,
					pool.get("java.lang.Thread"),
					clientThreadClsName,
					outDir);
			if(cls == null)
			{
				return false;
			}
			else
			{
				addPackage(clientThreadClsName);
			}
			
			String winFrameClsName = "com.ami.kvm.jviewer.gui.WindowCloseEvent";
			cls = createCls(WindowCloseEvent.class,
					pool.get("java.awt.event.WindowAdapter"),
					winFrameClsName,
					outDir);
			if(cls == null)
			{
				return false;
			}
			else
			{
				CtMethod method = cls.getDeclaredMethod("close", null);
				if(method != null)
				{
					cls.defrost();
					method.setBody("{new com.ami.kvm.jviewer.Client().clear();\n}");
					
					cls.writeFile(outDir);
				}
				
				addPackage(winFrameClsName);
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
//		private ArrayList<Integer>	ids	= new ArrayList<Integer>();
		private static String	ip;
		private static String	targetIp;
		private static int		port;
		private static List<String> cmdList = new ArrayList<String>();
		
		private void log(CharSequence charSeq)
		{
			System.out.println(charSeq);
		}

//		private void renewal(String servUrl, int servP)
//		{
//			ClientThread thread = new ClientThread();
//
//			thread.setHost(ip);
//			thread.setPort(port);
//			thread.setIds(ids);
//			thread.setName("client thread");
//			thread.start();
//		}

		public void addRules(String servUrl, String servP, String targetUrl)
				throws UnknownHostException, IOException
		{
			ip = servUrl;
			port = Integer.parseInt(servP);
			targetIp = targetUrl;

			log("serverUrl : " + servUrl + "; serverPort : " + servP + "; targetUrl : " + targetUrl);
//			ids.addAll(addRule(ip, port, targetUrl, "5900", "6"));
//			ids.addAll(addRule(ip, port, targetUrl, "5901", "6"));
//			ids.addAll(addRule(ip, port, targetUrl, "623", "17"));

//			renewal(ip, port);
			
			sendCmd(true);
		}
		
		public boolean sendCmd(boolean append)
		{
			log("ip : " + ip + "; port : " + port);
			
			Socket socket = new Socket();
			SocketAddress address = new InetSocketAddress(ip, port);
			String type = "delete";
			
			if(append)
			{
				type = "append";
			}
			
			for(int i = 0; i < 3; i++)
			{
				try
				{
					socket.setSoTimeout(5000);
					socket.connect(address, 3000);
					
					break;
				}
				catch (SocketTimeoutException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					
					return false;
				}
			}
			
			cmdInit();
			
			OutputStream outStream = null;
			InputStream inStream = null;
			byte[] buffer = new byte[1024];
			
			try
			{
				outStream = socket.getOutputStream();
				inStream = socket.getInputStream();
				
				log("begin send cmd list. size : " + cmdList.size());
				
				for(String cmd : cmdList)
				{
					outStream.write(type.getBytes());
					if(inStream.read(buffer) <= 0)
					{
						log("send cmd type error.");
						return false;
					}
					
					outStream.write(cmd.getBytes());
					if(inStream.read(buffer) <= 0)
					{
						log("send cmd error.");
						return false;
					}
					
					log("sended : " + cmd);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
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
			
			String ipAddr = getLocalIp();
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ ipAddr
					+ " --dport 5900 --jump DNAT --to-destination "
					+ targetIp + ":5900");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol tcp --source "
					+ ipAddr
					+ " --dport 5901 --jump DNAT --to-destination "
					+ targetIp + ":5901");
			
			cmdList.add("iptables --table nat --append PREROUTING --protocol udp --source "
					+ ipAddr
					+ " --dport 623 --jump DNAT --to-destination "
					+ targetIp + ":623");
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ ipAddr
					+ " --dport 5900 --jump SNAT --to-source 192.168.0.10");
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol tcp --source "
					+ ipAddr
					+ " --dport 5901 --jump SNAT --to-source 192.168.0.10");
			
			cmdList.add("iptables --table nat --append POSTROUTING --protocol udp --source "
					+ ipAddr
					+ " --dport 623 --jump SNAT --to-source 192.168.0.10");
		}

		private String getLocalIp()
		{
			Enumeration<NetworkInterface> inters;
			try
			{
				inters = NetworkInterface.getNetworkInterfaces();
				while(inters.hasMoreElements())
				{
					NetworkInterface inter = inters.nextElement();
					for(InterfaceAddress addr : inter.getInterfaceAddresses())
					{
						InetAddress add = addr.getAddress();
						
						if(add instanceof Inet4Address && !add.isLoopbackAddress())
						{
							return add.getHostAddress();
						}
					}
				}
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Deprecated
		@SuppressWarnings(value = { "unused" })
		private ArrayList<Integer> addRule(String url, int servP,
				String targetUrl, String port, String pro)
				throws UnknownHostException, IOException
		{
			log("prepare to connect : " + url + "; port : " + servP);
			
			Socket socket = new Socket(url, servP);
			
			log("connected.");

			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			byte[] b = new byte[1024];
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			log("ready to talk.");

			os.write("ip".getBytes());

			int len = is.read(b);
			String str = new String(b, 0, len);
			log("receive : " + str);
			if ("ok".equals(str))
			{
				os.write(targetUrl.getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("continue".equals(str))
			{
				os.write("port".getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("ok".equals(str))
			{
				os.write(port.getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("continue".equals(str))
			{
				os.write("protocol".getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("ok".equals(str))
			{
				os.write(pro.getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("continue".equals(str))
			{
				os.write("over".getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			if ("id".equals(str))
			{
				os.write("ok".getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			log("receive : " + str);
			ids.add(Integer.valueOf(Integer.parseInt(str)));

			len = is.read(b);
			str = new String(b, 0, len);
			if ("id".equals(str))
			{
				os.write("ok".getBytes());
			}

			len = is.read(b);
			str = new String(b, 0, len);
			ids.add(Integer.valueOf(Integer.parseInt(str)));

			len = is.read(b);
			str = new String(b, 0, len);
			if ("continue".equals(str))
			{
				os.write("end".getBytes());
			}
			
			log("add rule over.");

			return ids;
		}

		public void clear()
		{
//			Socket socket = new Socket(ip, port);
//
//			OutputStream os = socket.getOutputStream();
//			InputStream is = socket.getInputStream();
//
//			byte[] b = new byte[1024];
//
//			for (Integer id : ids)
//			{
//				os.write("kill".getBytes());
//
//				int len = is.read(b);
//				String str = new String(b, 0, len);
//				if ("ok".equals(str))
//				{
//					os.write(id.toString().getBytes());
//				}
//
//				len = is.read(b);
//				str = new String(b, 0, len);
//				if (!"continue".equals(str))
//				{
//					continue;
//				}
//			}
//
//			os.write("end".getBytes());
			
			sendCmd(false);
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

					Socket socket = new Socket(host, port);

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
	
	class WindowCloseEvent extends WindowAdapter
	{
		@Override
		public void windowClosing(WindowEvent event)
		{
			close();
		}
		
		private void close()
		{
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
	
	public static void main(String[] args) throws Exception
	{
//		ClassModify.Client client = new ClassModify.Client();
//		
//		client.addRules("10.0.31.53", "9000", "192.168.0.12");
		
//		Enumeration<NetworkInterface> inters = NetworkInterface.getNetworkInterfaces();
//		while(inters.hasMoreElements())
//		{
//			NetworkInterface inter = inters.nextElement();
//			for(InterfaceAddress addr : inter.getInterfaceAddresses())
//			{
//				InetAddress add = addr.getAddress();
//				
//				if(add instanceof Inet4Address && !add.isLoopbackAddress())
//				{
//					System.out.println(add.getHostAddress());
//				}
//			}
//		}
//		System.out.println();
		
//		Process result = Runtime.getRuntime().exec("ipconfig");
//		
//		InputStream inStream = result.getInputStream();
//		InputStream errStream = result.getErrorStream();	

//		System.out.println(inStream.available());
//		System.out.println(errStream.available());
		
//		byte[] buffer = new byte[1024];
//		int len = -1;
//		while((len = inStream.read(buffer)) > 0)
//		{
//			System.out.println(new String(buffer, 0, len));
//		}
		
//		int val = result.waitFor();
//		result.destroy();
		
//		int val = result.exitValue();
		
//		System.out.println(val);
		
		Base64 base64 = new Base64();
		MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		FileInputStream inStream = new FileInputStream(new File("d:/JViewer_-1938924485.jar"));
		
		byte[] buffer = new byte[100];
		int len = -1;
		while((len = inStream.read(buffer)) != -1)
		{
			messageDigest.update(buffer, 0, len);
		}
		byte[] digest = messageDigest.digest();
		for(int i = 0; i < digest.length; i++)
		{
			digest[i] = (byte) (digest[i] & 0xff);
		}
		inStream.close();
		System.out.println();
		
		digest = base64.encode(digest);
		for(byte dig : digest)
		{
			System.out.print((char)(dig & 0xff));
		}
	}
}
