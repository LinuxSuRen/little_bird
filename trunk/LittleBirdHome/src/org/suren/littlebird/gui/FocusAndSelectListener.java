package org.suren.littlebird.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class FocusAndSelectListener implements ActionListener
{

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if(source instanceof JComponent)
		{
			((JComponent) source).grabFocus();
		}
		
		if(source instanceof JComboBox)
		{
			((JComboBox) source).getEditor().selectAll();
		}
		else if(source instanceof JTextField)
		{
			((JTextField) source).selectAll();
		}
	}

}
