package org.suren.littlebird.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map.Entry;

public class RemoteLoggerServer extends UnicastRemoteObject implements
		LoggerServer
{
	private static final long	serialVersionUID	= 1L;

	public RemoteLoggerServer() throws RemoteException
	{
		super();
	}

	public String getName() throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getName();
	}

	public List<String> getNames() throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getNames();
	}

	public List<String> searchBy(String search) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.searchBy(search);
	}

	public String getLevel(String name) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getLevel(name);
	}

	public boolean setLevel(String name, String level) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.setLevel(name, level);
	}

	public boolean addBridge(String name, String host, int port)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.addBridge(name, host, port);
	}

	public boolean removeBridge(String name, String host, int port)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.removeBridge(name, host, port);
	}

	public int clearBridges(String name) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.clearBridges(name);
	}

	public int clearBridges() throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.clearBridges();
	}

	public List<String> getBridges(String name) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getBridges(name);
	}

	public List<String> getBridges() throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getBridges();
	}

	public List<Entry<String, String>> bridgeInfo(String loggerName,
			String bridgeName) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.bridgeInfo(loggerName, bridgeName);
	}

	public boolean addThreadFilter(String loggerName, String bridgeName,
			String threadName) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.addThreadFilter(loggerName, bridgeName, threadName);
	}

	public String getThreadFilter(String loggerName, String bridgeName)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getThreadFilter(loggerName, bridgeName);
	}

	public boolean addLevelMatchFilter(String loggerName, String bridgeName,
			String level) throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.addLevelMatchFilter(loggerName, bridgeName, level);
	}

	public String getLevelMatchFilter(String loggerName, String bridgeName)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getLevelMatchFilter(loggerName, bridgeName);
	}

	public boolean addStrMatchFilter(String loggerName, String bridgeName,
			String match)
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.addStrMatchFilter(loggerName, bridgeName, match);
	}

	public String getStrMatchFilter(String loggerName, String bridgeName)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getStrMatchFilter(loggerName, bridgeName);
	}

	public boolean clearFilter(String loggerName, String bridgeName)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.clearFilter(loggerName, bridgeName);
	}

	public List<List<Entry<String, String>>> getAllLoggers() throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.getAllLoggers();
	}

	public List<List<Entry<String, String>>> searchLoggersBy(String search)
			throws Exception
	{
		DefaultLoggerServer server = new DefaultLoggerServer();

		return server.searchLoggersBy(search);
	}
}
