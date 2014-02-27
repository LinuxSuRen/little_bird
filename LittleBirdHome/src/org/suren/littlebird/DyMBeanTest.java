package org.suren.littlebird;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

public class DyMBeanTest implements DynamicMBean
{

	@Override
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException
	{
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException
	{
	}

	@Override
	public AttributeList getAttributes(String[] attributes)
	{
		return null;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes)
	{
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException
	{
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		MBeanInfo beanInfo = new MBeanInfo(this.getClass().getName(), "this description",
				new MBeanAttributeInfo[]{}, new MBeanConstructorInfo[]{}
		, null, null);
		
		return beanInfo;
	}

}
