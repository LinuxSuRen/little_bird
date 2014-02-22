package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;

public interface BundleServer extends Serializable
{
	public List<SuRenBundle> getAll();
	public List<SuRenBundle> searchBy(String match);
	public List<SuRenBundle> matchBy(String regex);
	public List<SuRenBundle> matchBy(String regex, boolean insensitive);
	public List<SuRenBundle> matchBy(String regex, int flag);
	public SuRenBundle getById(long id);

	public int start(long ... ids);
	public int stop(long ... ids);
	public int update(long ... ids);

	public int install(String ... paths);
	public int uninstall(long ... ids);
}
