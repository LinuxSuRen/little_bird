package org.suren.jar;

/**
 * @author suren
 *
 */
public class Param
{
	private String outDir;
	private String targetJar;
	private String digest;
	private String mainCls;
	private String comment;
	
	public String getOutDir()
	{
		return outDir;
	}
	public void setOutDir(String outDir)
	{
		this.outDir = outDir;
	}
	public String getTargetJar()
	{
		return targetJar;
	}
	public void setTargetJar(String targetJar)
	{
		this.targetJar = targetJar;
	}
	public String getDigest()
	{
		return digest;
	}
	public void setDigest(String digest)
	{
		this.digest = digest;
	}
	public String getMainCls()
	{
		return mainCls;
	}
	public void setMainCls(String mainCls)
	{
		this.mainCls = mainCls;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
}
