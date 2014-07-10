/**
 *
 */
package org.suren.littlebird.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * @author suren
 *
 */
public class TabInfo
{
	private String title;
	private String server;
	private Map<String, Object> item = new HashMap<String, Object>();

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the server
	 */
	public String getServer()
	{
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server)
	{
		this.server = server;
	}

	/**
	 * @return the item
	 */
	public Map<String, Object> getItem()
	{
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Map<String, Object> item)
	{
		this.item = item;
	}

	public void putItem(String name, Object item)
	{
		if(getItem() == null)
		{
			setItem(new HashMap<String, Object>());
		}

		getItem().put(name, item);
	}
}
