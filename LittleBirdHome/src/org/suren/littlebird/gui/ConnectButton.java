package org.suren.littlebird.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.text.JTextComponent;

public class ConnectButton extends JButton
{

	private static final long	serialVersionUID	= 5407720117168934347L;
	
	private boolean connected;
	private String connectText = "connect";
	private String disconnectText = "disconnect";
	
	private JTextComponent user;
	private JTextComponent host;
	private JTextComponent port;
	private JTextComponent password;
	
	private Map<String, Object> data;
	
	public ConnectButton()
	{
		setConnected(false);
	}
	
	public ConnectButton(String text)
	{
		setText(text);
	}
	
	public JTextComponent getUser()
	{
		return user;
	}
	public void setUser(JTextComponent user)
	{
		this.user = user;
	}
	public JTextComponent getHost()
	{
		return host;
	}
	public void setHost(JTextComponent host)
	{
		this.host = host;
	}
	public JTextComponent getPort()
	{
		return port;
	}
	public void setPort(JTextComponent port)
	{
		this.port = port;
	}
	public JTextComponent getPassword()
	{
		return password;
	}
	public void setPassword(JTextComponent password)
	{
		this.password = password;
	}

	public Map<String, Object> getData()
	{
		return data;
	}

	public void setData(String key, Object value)
	{
		if(data == null)
		{
			data = new HashMap<String, Object>();
		}
		
		data.put(key, value);
	}

	public boolean isConnected()
	{
		return connected;
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
		
		if(connected)
		{
			setText(getDisconnectText());
		}
		else
		{
			setText(getConnectText());
		}
	}

	public String getConnectText()
	{
		return connectText;
	}

	public void setConnectText(String connectText)
	{
		this.connectText = connectText;
	}

	public String getDisconnectText()
	{
		return disconnectText;
	}

	public void setDisconnectText(String disconnectText)
	{
		this.disconnectText = disconnectText;
	}
}
