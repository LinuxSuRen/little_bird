package org.suren.littlebird.log;

import java.util.HashSet;
import java.util.Set;

public abstract class DefaultAppender implements ArchAppender
{
	private Set<String> classFilter = new HashSet<String>();
	
	@Override
	public abstract void append(CharSequence charSeq);

	@Override
	public void append(CharSequence charSeq, StackTraceElement stackTrace)
	{
		String className = stackTrace.getClassName();
		
		if(classFilter.contains(className))
		{
			append(charSeq);
		}
	}
	
	public boolean addFilter(String name)
	{
		return classFilter.add(name);
	}
	
	public boolean removeFilter(String name)
	{
		return classFilter.remove(name);
	}
}
