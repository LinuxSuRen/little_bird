package org.suren.littlebird.util;

import java.util.Comparator;

public class NumberComparator implements Comparator<Number>
{
	private static NumberComparator comparator;
	
	public static synchronized NumberComparator getInstance()
	{
		if(comparator == null)
		{
			comparator = new NumberComparator();
		}
		
		return comparator;
	}

	@Override
	public int compare(Number num1, Number num2)
	{
		long num1Val = num1.longValue();
		long num2Val = num2.longValue();
		
		if(num1Val > num2Val)
		{
			return 1;
		}
		else if(num1Val == num2Val)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
}
