package org.suren.arch;

import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StringUtil
{
	public static String omit(String str, int left, String flag)
	{
		if(empty(flag))
		{
			 flag = "...";
		}
		
		if(StringUtil.empty(str))
		{
			return str;
		}
		
		str = str.trim();
		
		left = left <= 0 ? 16 : left;
		
		if(left > str.length())
		{
			return str;
		}
		else
		{
			return str.substring(0, left) + flag;
		}
	}

	public static String omit(String str, int left)
	{
		return omit(str, left, null);
	}
	
	public static String omit(CharSequence seq, int left)
	{
		return omit(seq.toString(), left);
	}

	public static String omit(String str)
	{
		return omit(str, -1, null);
	}
	
	public static int indexOf(String str, String sub)
	{
		if(str == null)
		{
			return -1;
		}
		
		return str.indexOf(sub);
	}
	
	public static boolean contain(String str, String sub)
	{
		return indexOf(str, sub) != -1;
	}
	
	/**
	 * 判断字符串是否为空，如果为空则返回指定默认值
	 * @param str
	 * @param def 默认值
	 * @return
	 */
	public static String emptyDef(String str, String def)
	{
		if(empty(str))
		{
			str = def;
		}
		
		return str;
	}
	
	/**
	 * 判断是否为空字符串（为null或者内容为空）
	 * @param str
	 * @return 为空返回true
	 */
	public static boolean empty(String str)
	{
		return str == null || "".equals(str);
	}
	
	/**
	 * 判断n个字符串是否为空
	 * @param strs
	 * @return 任何一个字符串为空则返回true
	 */
	public static boolean empty(String ...strs)
	{
		if(strs == null)
		{
			return true;
		}
		
		for(String str : strs)
		{
			if(empty(str))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isNotEmpty(String str)
	{
		return !empty(str);
	}
	
	public static boolean equal(String str_a, String str_b)
	{
		return str_a == str_b || (str_a != null && str_a.equals(str_b));
	}
	
	public static boolean equalOneOf(String str_a, String ...args)
	{
		if(str_a == null && args == null)
		{
			return true;
		}
		
		for(String arg : args)
		{
			if(equal(str_a, arg))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public  static boolean equal(String str, Number enum_num)
	{
		return equal(str, enum_num + "");
	}
	
	public static boolean isNotEqual(String str_a, String str_b)
	{
		return !equal(str_a, str_b);
	}
	
	public static boolean isNotEqual(String str, Number enum_num)
	{
		return isNotEqual(str, enum_num + "");
	}
	
	public static String timeToStr(long time, String format)
	{
		if(empty(format))
		{
			format = "yyyy-MM-dd HH:mm:ss";
		}
		
		return new SimpleDateFormat(format).format(new Date(time));
	}
	
	public static String timeToStr(long time)
	{
		return timeToStr(time, null);
	}
	
	public static String timeToHuman(long time)
	{
		StringBuilder builder = new StringBuilder(); 
		
		if(time >= 86400000)
		{
			builder.append(time / 86400000);
			time %= 86400000;
		}
		else
		{
			builder.append("0");
		}
		builder.append(":");
		
		if(time >= 3600000)
		{
			builder.append(time / 3600000);
			time %= 3600000;
		}
		else
		{
			builder.append("0");
		}
		builder.append(":");
		
		if(time >= 60000)
		{
			builder.append(time / 60000);
			time %= 60000;
		}
		else
		{
			builder.append("0");
		}
		builder.append(":");
		
		if(time >= 1000)
		{
			builder.append(time / 1000);
		}
		else
		{
			builder.append("0");
		}
		
		return builder.toString();
	}
	
	public static String byteToHuman(long size)
	{
		StringBuilder builder = new StringBuilder();
		
		if(size >= 1073741824)
		{
			builder.append((size * 100 / 1073741824) / 100.0);
			builder.append("GB");
		}
		else if(size >= 1048576)
		{
			builder.append((size *100 / 1048576) / 100.0);
			builder.append("MB");
		}
		else if(size >= 1024)
		{
			builder.append((size * 100 / 1024) / 100.0);
			builder.append("KB");
		}
		else
		{
			builder.append(size < 0 ? 0 : size);
			builder.append("B");
		}
		
		return builder.toString();
	}
	
	public static String timeToStr(String time)
	{
		try
		{
			long misc = Long.parseLong(time);
			
			time = timeToStr(misc);
		}
		catch(NumberFormatException e)
		{
		}
		
		return time;
	}

	public static String timeToMillis(Date time)
	{
		Calendar calendar = Calendar.getInstance();
		
		if(time == null)
		{
			return "";
		}
		
		calendar.setTime(time);
		
		return calendar.getTimeInMillis() + "";
	}
	
	public static String ipIntToStr(int ip)
	{
		try
		{
			byte[] bytes = new byte[4];
			
			bytes[0] = (byte) (0xff & ip);
			bytes[1] = (byte) ((0xff00 & ip) >> 8);
			bytes[2] = (byte) ((0xff0000 & ip) >> 16);
			bytes[3] = (byte) ((0xff000000 & ip) >> 24);
			
			return Inet4Address.getByAddress(bytes).getHostAddress();
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	public static int getInt(String str, int def)
	{
		try
		{
			return Integer.parseInt(str);
		}
		catch(NumberFormatException e)
		{
			return def;
		}
	}
	
	public static int getInt(CharSequence charSeq, int def)
	{
		if(charSeq != null)
		{
			return getInt(charSeq.toString(), def);
		}
		else
		{
			return def;
		}
	}
}
