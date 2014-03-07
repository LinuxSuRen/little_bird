package org.suren.littlebird.server;

import org.suren.littlebird.ArchServerListener;

public interface LogServerListener extends ArchServerListener
{
	public void printMessage(CharSequence msg);
}
