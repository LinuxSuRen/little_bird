package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;

public interface LoggerServer extends Serializable, Server
{
	public List<String> getNames();
	public List<String> searchBy(String search);

	public String getLevel(String name);
	public boolean setLevel(String name, String level);
}
