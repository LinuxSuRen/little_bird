package org.suren.littlebird.log;

public interface ArchAppender
{
	public void append(CharSequence charSeq);
	
	public void append(CharSequence charSeq, StackTraceElement stackTrace);
}
