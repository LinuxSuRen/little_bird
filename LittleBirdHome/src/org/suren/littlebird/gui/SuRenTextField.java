package org.suren.littlebird.gui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class SuRenTextField extends JTextField
{

	private static final long	serialVersionUID	= 1L;

	public SuRenTextField()
	{
		super();
	}

	public SuRenTextField(Document doc, String text, int columns)
	{
		super(doc, text, columns);
	}

	public SuRenTextField(int columns)
	{
		super(columns);
	}

	public SuRenTextField(String text, int columns)
	{
		super(text, columns);
	}

	public SuRenTextField(String text)
	{
		super(text);
	}
	
	public void addComboText(final SuRenTextField field)
	{
		getDocument().addDocumentListener(new DocumentListener()
		{
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				change(e);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				change(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
			}
			
			private void change(DocumentEvent e)
			{
				field.setText(getText());
			}
		});
	}

}
