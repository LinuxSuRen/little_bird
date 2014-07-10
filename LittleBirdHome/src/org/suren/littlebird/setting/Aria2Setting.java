/**
 *
 */
package org.suren.littlebird.setting;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author suren
 *
 */
@XmlRootElement
public class Aria2Setting
{
	private Set<String> server;

	/**
	 * @return the server
	 */
	@XmlElement
	public Set<String> getServer()
	{
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(Set<String> server)
	{
		this.server = server;
	}
}
