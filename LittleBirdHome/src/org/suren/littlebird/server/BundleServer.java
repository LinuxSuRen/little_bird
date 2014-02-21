package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;

public interface BundleServer extends Serializable
{
	public String hello();

	public List<SuRenBundle> getAll();
	public SuRenBundle getById(long id);

	public int start(long ... ids);
	public int stop(long ... ids);

	public int install(String ... paths);
	public int uninstall(long ... ids);
}
