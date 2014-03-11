package org.suren.littlebird.server;

import java.io.Serializable;
import java.util.List;

import org.suren.littlebird.po.SuRenBundle;

public interface BundleServer extends Serializable, Server
{
	public List<SuRenBundle> getAll();
	public List<SuRenBundle> searchBy(String search);
	public List<SuRenBundle> matchBy(String regex);
	public List<SuRenBundle> matchBy(String regex, boolean insensitive);
	public List<SuRenBundle> matchBy(String regex, int flag);
	public SuRenBundle getById(long id);

	public int start(long ... ids);
	public int stop(long ... ids);
	public int update(long ... ids);
	
	public int setStartLevel(int level, long ... ids);

	public int install(String ... paths);
	public int uninstall(long ... ids);
}
