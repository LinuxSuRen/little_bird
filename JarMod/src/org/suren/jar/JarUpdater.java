package org.suren.jar;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.apache.commons.codec.binary.Base64;
import org.suren.cls.ClassModify;

/**
 * @author suren
 *
 */
public class JarUpdater
{
	private MessageDigest messageDigest;
	private Base64 base64 = new Base64();

	private static final String CMD = "cmd";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		JarUpdater jarUpdater = new JarUpdater();

		Param param = jarUpdater.paramParse(args);
		if(param == null)
		{
			jarUpdater.usagePrint();

			return;
		}

		boolean result = jarUpdater.modify(param);

		if(result && !CMD.equals(param.getJviewer()))
		{
			System.out.println("success");
		}

		System.out.println(param.getJviewer());
	}

	public boolean modify(Param param) throws Exception
	{
		if(param == null)
		{
			return false;
		}

		ClassModify clsModify = new ClassModify();

		String outDir = param.getOutDir();
		String targetJar = param.getTargetJar();
		String mainCls = param.getMainCls();

		File dstFile = backupTo(targetJar, outDir);
		if(dstFile == null && !CMD.equals(param.getJviewer()))
		{
			System.err.println("jar file backup error.");

			return false;
		}

		boolean modifyClsRes = clsModify.modify(targetJar, mainCls, outDir);
		if(!CMD.equals(param.getJviewer()))
		{
			System.out.println("modifyClsRes : " + modifyClsRes);
		}

		if(modifyClsRes)
		{
			List<String> packageList = clsModify.getPackageList();
			for(String packageName : packageList)
			{
				String clsPath = packageName.replace(".", "/") + ".class";

				cover(dstFile, new File(outDir, clsPath), clsPath, param);

				new File(outDir, clsPath).delete();
			}

			for(String packageName : packageList)
			{
				String clsPath = packageName.replace(".", "/") + ".class";

				clean(new File(outDir, clsPath).getParent());
			}
		}
		else
		{
			cover(dstFile, null, null, param);
		}

		if(!CMD.equals(param.getJviewer()))
		{
			System.out.println("result file is : " + dstFile.getName());
		}
		else
		{
			System.out.println(dstFile.getName());
		}

		param.setJviewer(dstFile.getAbsolutePath());

		return true;
	}

	public Param paramParse(String[] args)
	{
		Param param = null;

		if(args != null && args.length >= 2)
		{
			String[] paramArray = Arrays.copyOf(args, 6);

			param = new Param();

			int i = 0;

			param.setOutDir(paramArray[i++]);
			param.setTargetJar(paramArray[i++]);
			param.setJviewer(paramArray[i++]);
			param.setComment(paramArray[i++]);
			param.setDigest(paramArray[i++]);
			param.setMainCls(paramArray[i++]);
		}

		paramDef(param);

		return param;
	}

	private void paramDef(Param param)
	{
		if(param == null)
		{
			return;
		}

		String timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

		StringBuffer commentBuffer = new StringBuffer();
		commentBuffer.append("auto modify by suren\n");
		commentBuffer.append(timestamp).append("\n");
		commentBuffer.append("client v2.0.1399271108710");

		if(param.getComment() != null)
		{
			commentBuffer.append(param.getComment()).append("\n");
		}

		param.setComment(commentBuffer.toString());
	}

	private void usagePrint()
	{
		System.err.println("Usage:cmd outDir path comment mainCls");
	}

	private File backupTo(String src, String outDir)
	{
		int hashCode = new Generator().metaHash(src);
		if(hashCode == Generator.INVALID_HASH)
		{
			return null;
		}

		File srcFile = new File(src);
		File dstFile = new File(outDir, "JViewer_" + hashCode + ".jar");
		if(!srcFile.isFile())
		{
			return null;
		}

		File dstParent = dstFile.getParentFile();
		if((!dstParent.exists() && !dstParent.mkdirs()) || dstParent.isFile())
		{
			System.out.println("invalid dest path : " + dstFile.getAbsolutePath());
			return null;
		}

		boolean result = copy(srcFile, dstFile);

		if(result)
		{
			return dstFile;
		}
		else
		{
			return null;
		}
	}

	private void clean(String path)
	{
		if(path == null)
		{
			return;
		}

		File file = new File(path);
		String parent = file.getParent();

		if(file.delete())
		{
			clean(parent);
		}
	}

	public void cover(File src, File targetFile, String path, Param param) throws Exception
	{
		if(param == null)
		{
			System.err.println("no param.");

			return;
		}

		String comment = param.getComment();
		File tmpJarFile = File.createTempFile("update", "jarfile");
		tmpJarFile.deleteOnExit();

		JarInputStream jarIn = new JarInputStream(new FileInputStream(src));
		JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(tmpJarFile));
		Map<String, String> digestMap = new HashMap<String, String>();

		comment = comment == null ? "" : comment;
		JarEntry entry = null;
		boolean notFound = true;
		StringBuffer buffer = null;

		if("digest".equals(param.getDigest()))
		{
			buffer = new StringBuffer();
		}

		while((entry = jarIn.getNextJarEntry()) != null)
		{
			String entryName = entry.getName();
			if(entryName.indexOf("META-INF") != -1 &&
					!entryName.equals(JarFile.MANIFEST_NAME))
			{
				continue;
			}

			jarOut.putNextEntry(entry);
			if(entry.getName().equals(path))
			{
				FileInputStream inStream = new FileInputStream(targetFile);

				update(jarIn, null);
				update(inStream, jarOut, buffer);

				ioClose(inStream);

				notFound = false;
			}
			else
			{
				update(jarIn, jarOut, buffer);
			}

			if(buffer != null && buffer.length() > 0)
			{
				digestMap.put(entryName, buffer.toString());
			}
		}

		Manifest manifest = jarIn.getManifest();
		if(manifest != null)
		{
			ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
			jarOut.putNextEntry(e);

			Map<String, Attributes> entries = manifest.getEntries();
			entries.clear();

			Set<String> keys = digestMap.keySet();
			for(String key : keys)
			{
				Attributes attr = new Attributes();
				attr.putValue("SHA1-Digest", digestMap.get(key));

				entries.put(key, attr);
			}

			if(param != null)
			{
				Attributes surenAttr = new Attributes();
				surenAttr.putValue("comment", param.getComment().replaceAll("\n", "; "));

				entries.put("suren", surenAttr);
			}

			manifest.write(jarOut);
		}

		if(notFound && path != null)
		{
			JarEntry jarEntry = new JarEntry(path);

			try
			{
				jarOut.putNextEntry(jarEntry);

				FileInputStream inStream = new FileInputStream(targetFile);
				update(inStream, jarOut);
				ioClose(inStream);
			}
			catch(ZipException e)
			{
				e.printStackTrace();
			}
		}

		jarOut.setComment(comment);

		jarIn.close();
		jarOut.close();

		copy(tmpJarFile, src);
	}

	public void update(InputStream inStream, OutputStream outStream, StringBuffer buffer) throws IOException
	{
		if(inStream == null)
		{
			return;
		}

		byte[] buf = new byte[1024];
		int len = -1;

		if(messageDigest == null)
		{
			try
			{
				messageDigest = MessageDigest.getInstance("SHA1");
			}
			catch(NoSuchAlgorithmException e)
			{
				e.printStackTrace();

				return;
			}
		}

		messageDigest.reset();
		while((len = inStream.read(buf)) != -1)
		{
			if(buffer != null)
			{
				messageDigest.update(buf, 0, len);
			}

			if(outStream != null)
			{
				outStream.write(buf, 0, len);
			}
		}

		if(buffer != null)
		{
			buffer.delete(0, buffer.length());

			byte[] digest = base64.encode(messageDigest.digest());
			for(byte dig : digest)
			{
				buffer.append((char)(dig & 0xff));
			}
		}
	}

	public void update(InputStream inStream, OutputStream outStream) throws IOException
	{
		update(inStream, outStream, null);
	}

	private boolean copy(File src, File target)
	{
		if(src == null || target == null)
		{
			return false;
		}

		FileInputStream inStream = null;
		FileOutputStream outStream = null;

		try
		{
			inStream = new FileInputStream(src);
			outStream = new FileOutputStream(target);

			update(inStream, outStream);
		}
		catch(Exception e)
		{
			e.printStackTrace();

			return false;
		}
		finally
		{
			ioClose(inStream, outStream);
		}

		return true;
	}

	private void ioClose(Closeable ... streams)
	{
		if(streams != null)
		{
			for(Closeable stream : streams)
			{
				if(stream == null)
				{
					continue;
				}

				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
