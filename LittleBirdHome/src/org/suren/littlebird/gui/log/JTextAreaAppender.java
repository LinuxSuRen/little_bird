package org.suren.littlebird.gui.log;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.suren.littlebird.log.DefaultAppender;

public class JTextAreaAppender extends DefaultAppender
{
	private JTextArea targetArea;
	public final int MAX_ROWS = 10000;
	public final int MIN_ROWS = 1;
	private int rowsLimit = 500;
	private boolean autoScoll = true;
	
	@Override
	public synchronized void append(CharSequence charSeq)
	{
		if(charSeq == null || targetArea == null)
		{
			return;
		}
		
		targetArea.append(charSeq.toString());
		
		if(autoScoll)
		{
			targetArea.setCaretPosition(targetArea.getText().length() - 1);
		}
		
		int leftRows = rowsLimit - targetArea.getLineCount();
		if(leftRows < 0)
		{
			try
			{
				int end = targetArea.getLineStartOffset(-leftRows);
				targetArea.getDocument().remove(0, end);
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void appendLine(CharSequence charSeq)
	{
		StringBuffer buffer = new StringBuffer(charSeq);
		
		buffer.append("\n");
		
		append(buffer);
	}

	public JTextArea getTargetArea()
	{
		return targetArea;
	}

	public void setTargetArea(JTextArea targetArea)
	{
		this.targetArea = targetArea;
	}

	public int getRowsLimit()
	{
		return rowsLimit;
	}

	public void setRowsLimit(int rowsLimit)
	{
		if(rowsLimit < MIN_ROWS || rowsLimit > MAX_ROWS)
		{
			return;
		}
		
		this.rowsLimit = rowsLimit;
	}

	public boolean isAutoScoll()
	{
		return autoScoll;
	}

	public void setAutoScoll(boolean autoScoll)
	{
		this.autoScoll = autoScoll;
	}
}