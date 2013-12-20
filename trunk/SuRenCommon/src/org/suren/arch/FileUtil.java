package org.suren.arch;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtil
{
	public static boolean copy(String src, String dst)
	{
		FileInputStream srcStream = null;
		FileOutputStream dstStream = null;
		try
		{
			File srcFile = new File(src);
			
			if(!srcFile.isFile())
			{
				return false;
			}
			
			srcStream = new FileInputStream(srcFile);
			dstStream = new FileOutputStream(new File(dst));
			
			byte[] buffer = new byte[1024];
			int len = -1;
			while((len = srcStream.read(buffer)) > 0)
			{
				dstStream.write(buffer, 0, len);
			}
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			try
			{
				if(srcStream != null)
				{
					srcStream.close();
				}
				
				if(dstStream != null)
				{
					dstStream.close();
				}
			}
			catch (IOException e)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean copy(File srcFile, File dstFile)
	{
		if(srcFile == null || dstFile == null)
		{
			return false;
		}
		
		return copy(srcFile.getAbsolutePath(), dstFile.getAbsolutePath());
	}
	
	public static void delFile(String dir)
	{
		File file = new File(dir);
		
		if(file.exists())
		{
			if(file.isDirectory())
			{
				File[] files = file.listFiles();
				if(files != null)
				{
					for(File delFile : files)
					{
						delFile(delFile.getAbsolutePath());
					}
				}
			}
			else
			{
				file.delete();
			}
		}
	}
	
	public static void findBySuffix(File root, String suffix, ArrayList<File> result, int level)
	{
		if(root.isDirectory() && root.getAbsolutePath().split("/").length <= level)
		{
			File[] subFiles = root.listFiles();
			if(subFiles != null)
			{
				for(File file : subFiles)
				{
					findBySuffix(file, suffix, result, level);
				}
			}
		}
		else if(root.isFile() && root.getName().endsWith(suffix))
		{
			synchronized (result) {
				result.add(root);
				
				result.notify();
			}
		}
	}
	
	public static void findApk(ArrayList<File> result, int level)
	{
		findBySuffix(new File("/"), ".apk", result, level);
	}
	
	public static void findApk(ArrayList<File> result)
	{
		findApk(result, 5);
	}
	
	public static boolean createFile(File file)
	{
		boolean result = false;
		
		try
		{
			result = file.createNewFile();
		}
		catch(Exception e)
		{
			result = false;
		}
		
		return result;
	}
	
	public static boolean putInFile(CharSequence buffer, File file, boolean force)
	{
		boolean result = false;
		FileOutputStream fileWriter = null;
		
		if(file == null)
		{
			return result;
		}
		
		if(force)
		{
			createFile(file);
		}
		else if(file.exists())
		{
			return result;
		}
		else
		{
			createFile(file);
		}
		
		try
		{
			fileWriter = new FileOutputStream(file);
			
			String content = buffer.toString();
			fileWriter.write(content.getBytes());
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}
		finally
		{
			if(fileWriter != null)
			{
				try
				{
					fileWriter.close();
					
					result = true;
				}
				catch (IOException e)
				{
				}
			}
		}
		
		return result;
	}
	
	public static boolean putInFile(CharSequence buffer, File file)
	{
		return putInFile(buffer, file, false);
	}
	
	public static String trimInvalidChar(String name)
	{
		if(StringUtil.isNotEmpty(name))
		{
			name = name.replaceAll("[\\* \\\\ \\< \\> \\/ : \\| \" \\?]", "");
		}
		
		return name;
	}
	
	public static String suffix(File file)
	{
		if(file == null)
		{
			return null;
		}
		
		String suffix = file.getName();
		int index = suffix.lastIndexOf(".");
		
		if(index < 0 || index >= suffix.length() - 1)
		{
			return suffix;
		}
		
		suffix = suffix.substring(index + 1);
		
		return suffix;
	}
	
	public static boolean addHeader(File file, byte[] header)
	{
		boolean result = false;
		
		if(file == null || !file.isFile() || header == null)
		{
			return result;
		}
		
		FileOutputStream tempOut = null;
		File tempFile = null;
		
		try
		{
			tempFile = File.createTempFile(file.getName(), "addHeader");
			tempOut = new FileOutputStream(tempFile);
			tempOut.write(header, 0, header.length);
			
			return FileUtil.appendWithoutClose(tempOut, file) &&
					FileUtil.copy(tempFile, file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.close(tempOut);
			
			if(tempFile != null)
			{
				tempFile.delete();
			}
		}
		
		return result;
	}
	
	public static boolean appendWithoutClose(FileOutputStream outStream,
			File file)
	{
		if(outStream == null || file == null || !file.isFile()
				|| !file.canRead())
		{
			return false;
		}
		
		
		FileInputStream inStream = null;
		
		try
		{
			inStream = new FileInputStream(file);
			
			byte[] buffer = new byte[1024];
			int len = -1;
			
			while((len = inStream.read(buffer)) != -1)
			{
				outStream.write(buffer, 0, len);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.close(inStream);
		}
		
		return true;
	}
	
	public static void close(Closeable ... streams)
	{
		if(streams == null)
		{
			return;
		}
		
		for(Closeable stream : streams)
		{
			try {
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static byte[] getWavHeaer(long totalDataLen, int channels,
			long longSampleRate, long byteRate, long totalAudioLen)
	{
		byte[] header = new byte[44];
		
		header[0] = 'R';
		header[1] = 'I'; 
		header[2] = 'F'; 
		header[3] = 'F'; 
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff); 
		header[6] = (byte) ((totalDataLen >> 16) & 0xff); 
		header[7] = (byte) ((totalDataLen >> 24) & 0xff); 
		header[8] = 'W'; 
		header[9] = 'A'; 
		header[10] = 'V'; 
		header[11] = 'E'; 
		header[12] = 'f'; // 'fmt ' chunk 
		header[13] = 'm'; 
		header[14] = 't'; 
		header[15] = ' '; 
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk 
		header[17] = 0; 
		header[18] = 0; 
		header[19] = 0; 
		header[20] = 1; // format = 1 
		header[21] = 0; 
		header[22] = (byte) channels; 
		header[23] = 0; 
		header[24] = (byte) (longSampleRate & 0xff); 
		header[25] = (byte) ((longSampleRate >> 8) & 0xff); 
		header[26] = (byte) ((longSampleRate >> 16) & 0xff); 
		header[27] = (byte) ((longSampleRate >> 24) & 0xff); 
		header[28] = (byte) (byteRate & 0xff); 
		header[29] = (byte) ((byteRate >> 8) & 0xff); 
		header[30] = (byte) ((byteRate >> 16) & 0xff); 
		header[31] = (byte) ((byteRate >> 24) & 0xff); 
		header[32] = (byte) 2;//(2 * 16 / 8); // block align 
		header[33] = 0; 
		header[34] = 16; // bits per sample 
		header[35] = 0; 
		header[36] = 'd'; 
		header[37] = 'a'; 
		header[38] = 't'; 
		header[39] = 'a'; 
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff); 
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff); 
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff); 
       
		return header;
	}
}
