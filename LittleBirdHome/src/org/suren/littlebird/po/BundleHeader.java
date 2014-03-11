package org.suren.littlebird.po;

public class BundleHeader
{
	private Object key;
	private Object value;

	public Object getKey()
	{
		return key;
	}
	public void setKey(Object key)
	{
		this.key = key;
	}
	public Object getValue()
	{
		return value;
	}
	public void setValue(Object value)
	{
		this.value = value;
	}
	@Override
	public String toString()
	{
		return key + ": " +  value + ";";
	}
}
