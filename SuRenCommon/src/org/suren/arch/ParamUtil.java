package org.suren.arch;

public class ParamUtil
{
	public static boolean isAllEmpty(Object ...params)
	{
		if(params == null)
		{
			return true;
		}
		
		for(Object param : params)
		{
			if(param instanceof String && StringUtil.isNotEmpty((String)param))
			{
				return false;
			}
			
			if(param != null)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean hasEmpty(Object ...params)
	{
		if(params == null)
		{
			return true;
		}
		
		for(Object param : params)
		{
			if(param == null)
			{
				return true;
			}
			
			if(param instanceof String && StringUtil.empty((String)param))
			{
				return true;
			}
		}
		
		return false;
	}
}
