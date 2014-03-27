package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

public interface LoggerServer extends Serializable, Server
{
	public List<String> getNames() throws Exception;
	public List<String> searchBy(String search) throws Exception;

	public List<List<Entry<String, String>>> getAllLoggers() throws Exception;
	public List<List<Entry<String, String>>> searchLoggersBy(String search) throws Exception;

	public String getLevel(String name) throws Exception;
	public boolean setLevel(String name, String level) throws Exception;

	public boolean addBridge(String name, String host, int port) throws Exception;
	public boolean removeBridge(String name, String host, int port) throws Exception;
	public int clearBridges(String name) throws Exception;
	public int clearBridges() throws Exception;
	public List<String> getBridges(String name) throws Exception;
	public List<String> getBridges() throws Exception;

	public List<Entry<String, String>> bridgeInfo(String loggerName,
			String bridgeName) throws Exception;

	public boolean addThreadFilter(String loggerName, String bridgeName,
			String threadName) throws Exception;
	public String getThreadFilter(String loggerName, String bridgeName) throws Exception;

	public boolean addLevelMatchFilter(String loggerName, String bridgeName,
			String level) throws Exception;
	public String getLevelMatchFilter(String loggerName, String bridgeName) throws Exception;

	public boolean addStrMatchFilter(String loggerName, String bridgeName,
			String match);
	public String getStrMatchFilter(String loggerName, String bridgeName) throws Exception;

	public boolean clearFilter(String loggerName, String bridgeName) throws Exception;
}
