package org.suren.littlebird.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class SuRenStatusButton extends JButton
{

	private static final long	serialVersionUID	= 1L;
	
	private int status = -1;
	
	private Map<Integer, String> statusMap = new HashMap<Integer, String>();
	private Map<Integer, Object> targetMap = new HashMap<Integer, Object>();

	public SuRenStatusButton()
	{
		super();
	}

	public SuRenStatusButton(Action a)
	{
		super(a);
	}

	public SuRenStatusButton(Icon icon)
	{
		super(icon);
	}

	public SuRenStatusButton(String text, Icon icon)
	{
		super(text, icon);
	}

	public SuRenStatusButton(String text)
	{
		super(text);
	}
	
	public void addStatus(int code, String name)
	{
		statusMap.put(code, name);
	}
	
	public boolean setStatus(int code)
	{
		String name = statusMap.get(code);
		if(name != null)
		{
			setText(name);
			
			status = code;
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getStatus()
	{
		return status;
	}

	public Map<Integer, String> getStatusMap()
	{
		return statusMap;
	}
	
	public void addTarget(int code, Object target)
	{
		targetMap.put(code, target);
	}

	public Map<Integer, Object> getTargetMap()
	{
		return targetMap;
	}
	
	public Object getTarget(int code)
	{
		return targetMap.get(code);
	}
}
