package org.suren.littlebird.log;

public class ConsoleAppender extends DefaultAppender
{

	@Override
	public void append(CharSequence charSeq)
	{
		System.out.print(charSeq);
	}
}
