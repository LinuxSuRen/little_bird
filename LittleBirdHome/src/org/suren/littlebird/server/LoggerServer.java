package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;

public interface LoggerServer extends Serializable, Server
{
	public List<String> getNames();
	public List<String> searchBy(String search);

	public String getLevel(String name);
	public boolean setLevel(String name, String level);

	public boolean addBridge(String name, String host, int port);
	public boolean removeBridge(String name, String host, int port);
	public int clearBridges(String name);
	public int clearBridges();
	public List<String> getBridges(String name);
	public List<String> getBridges();
}
