package org.suren.littlebird.po;

import java.util.Map.Entry;

public class DataEntry<K, V> implements Entry<K, V>
{
	private K key;
	private V value;

	public DataEntry()
	{}

	public DataEntry(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	public void setKey(K key)
	{
		this.key = key;
	}

	public K getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}

	public V setValue(V value)
	{
		return value;
	}
}
