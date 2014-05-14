package org.suren.littlebird.util;

import javax.swing.JTextField;

public class NumberParser
{
	public static int parseTo(CharSequence chars, int def)
	{
		if(chars == null)
		{
			return def;
		}
		
		try
		{
			return Integer.parseInt(chars.toString());
		}
		catch(NumberFormatException e)
		{
			return def;
		}
	}
	
	public static int parseTo(JTextField text, int def)
	{
		if(text == null)
		{
			return def;
		}
		
		return NumberParser.parseTo(text.getText(), def);
	}
}
