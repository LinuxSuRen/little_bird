package org.suren.arch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;


public class MapUtil<K, V> extends HashMap<K, V>
{
	private static final long serialVersionUID = 9140985083466902214L;
	
	public MapUtil<K, V> only(K key, V def)
	{
		V value = this.get(key);
		
		if(value == null)
		{
			value = def;
		}
		
		this.clear();
		this.put(key, value);
		
		return this;
	}
	
	public void turnToObject(Object obj)
	{
		if(ParamUtil.hasEmpty(obj))
		{
			return;
		}
		
		Set<K> keys = keySet();
		Class<?> objCls = obj.getClass();
		
		for(K key : keys)
		{
			String strKey = key.toString();
			String methodName = "set" + strKey.substring(0, 1).toUpperCase(Locale.ENGLISH);
			
			if(strKey.length() > 1)
			{
				methodName += strKey.substring(1);
			}
			
			try {
				Method method = objCls.getMethod(methodName, String.class);
				
				method.invoke(obj, get(key));
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}
}
