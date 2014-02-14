package org.suren.littlebird.gui;

import java.awt.dnd.DropTarget;

public class GeneralDropTarget<T> extends DropTarget
{
	private static final long	serialVersionUID	= 993383662543559850L;
	private T targetObject;

	public T getTargetObject()
	{
		return targetObject;
	}

	public void setTargetObject(T targetObject)
	{
		this.targetObject = targetObject;
	}
}
