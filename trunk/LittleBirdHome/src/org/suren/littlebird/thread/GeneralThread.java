package org.suren.littlebird.thread;

import java.util.Map;

public class GeneralThread<T> extends Thread
{
	private T data;
	private Map<String, T> dataMap;
	
	public GeneralThread(String name)
	{
		this.setName(name);
	}

	public T getData()
	{
		return data;
	}

	public void setData(T data)
	{
		this.data = data;
	}

	public Map<String, T> getDataMap()
	{
		return dataMap;
	}

	public void setDataMap(Map<String, T> dataMap)
	{
		this.dataMap = dataMap;
	}
}
