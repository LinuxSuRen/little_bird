package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

public interface LoggerServer extends Serializable, Server
{
	public List<String> getNames();
	public List<String> searchBy(String search);

	public List<List<Entry<String, String>>> getAllLoggers();
	public List<List<Entry<String, String>>> searchLoggersBy(String search);

	public String getLevel(String name);
	public boolean setLevel(String name, String level);

	public boolean addBridge(String name, String host, int port);
	public boolean removeBridge(String name, String host, int port);
	public int clearBridges(String name);
	public int clearBridges();
	public List<String> getBridges(String name);
	public List<String> getBridges();

	public List<Entry<String, String>> bridgeInfo(String loggerName,
			String bridgeName);

	public boolean addFilter(String loggerName, String bridgeName,
			String threadName);
	public boolean clearFilter(String loggerName, String bridgeName);
}
