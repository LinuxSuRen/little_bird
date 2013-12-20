package org.suren.arch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class CommonConvert<K, V> {
	public V convert(K from, V to)
	{
		if(from == null || to == null)
		{
			return to;
		}
		
		Class<?> toClass = to.getClass();
		Class<?> fromClass = from.getClass();
		
		Field[] toFields = toClass.getDeclaredFields();
		for(Field field : toFields)
		{
			String name = field.getName();
			Class<?> type = field.getType();
			String prefix = name.substring(0, 1).toUpperCase(Locale.CHINESE);
			String suffix = name.length() > 1 ? name.substring(1) : "";
			
			name = prefix + suffix;
			
			String getName = "get" + name;
			String setName = "set" + name;
			Method fromMethod = null;
			Method toMethod = null;
			
			try {
				fromMethod = fromClass.getMethod(getName);
				toMethod = toClass.getMethod(setName, type);
				
				fromMethod.setAccessible(true);
				toMethod.setAccessible(true);
				
				toMethod.invoke(to, fromMethod.invoke(from));
			} catch (Exception e) {
				continue;
			}
		}
		
		return to;
	}
}
