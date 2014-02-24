package org.suren.littlebird.server;


public class SuRenBundle
{
	public static final int	UNINSTALLED				= 0x00000001;
	public static final int	INSTALLED				= 0x00000002;
	public static final int	RESOLVED				= 0x00000004;
	public static final int	STARTING				= 0x00000008;
	public static final int	STOPPING				= 0x00000010;
	public static final int	ACTIVE					= 0x00000020;
	public static final int	START_TRANSIENT			= 0x00000001;
	public static final int	START_ACTIVATION_POLICY	= 0x00000002;
	public static final int	STOP_TRANSIENT			= 0x00000001;

	private long id;
	private String name;
	private String location;
	private int state;
	private long lastModified;
	private String version;
	private BundleHeader[] headers;
	private ServiceInfo[] serviceRef;

	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getLocation()
	{
		return location;
	}
	public void setLocation(String location)
	{
		this.location = location;
	}
	public int getState()
	{
		return state;
	}
	public void setState(int state)
	{
		this.state = state;
	}
	public long getLastModified()
	{
		return lastModified;
	}
	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
	public BundleHeader[] getHeaders()
	{
		return headers;
	}
	public void setHeaders(BundleHeader[] headers)
	{
		this.headers = headers;
	}
	public ServiceInfo[] getServiceRef()
	{
		return serviceRef;
	}
	public void setServiceRef(ServiceInfo[] serviceRef)
	{
		this.serviceRef = serviceRef;
	}
	@Override
	public String toString()
	{
		return "SuRenBundle [id=" + id + ", name=" + name + ", location="
				+ location + ", state=" + state + ", lastModified="
				+ lastModified + ", version=" + version + "]";
	}
}
