package org.suren.jar;

/**
 * @author suren
 *
 */
public class Param
{
	private String	outDir;
	private String	targetJar;
	private String	mainCls;
	
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
	public String getMainCls()
	{
		return mainCls;
	}
	public void setMainCls(String mainCls)
	{
		this.mainCls = mainCls;
	}
}
