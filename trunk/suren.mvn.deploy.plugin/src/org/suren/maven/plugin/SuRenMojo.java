package org.suren.maven.plugin;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class SuRenMojo extends AbstractMojo
{

	@Parameter(property = "project")
	private MavenProject project;

	protected File getJar()
	{
		Build build = getProject().getBuild();
		String finalName = build.getFinalName();
		String dir = build.getDirectory();

		return new File(dir, finalName + ".jar");
	}

	public MavenProject getProject()
	{
		return project;
	}

	public void setProject(MavenProject project)
	{
		this.project = project;
	}
}
