package org.suren.littlebird.server;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class DefaultBundleServer  implements BundleServer
{
	private static final long	serialVersionUID	= 1L;
	private BundleContext context;

	private final int BundleStart = 0x1;
	private final int BundleStop = 0x2;
	private final int BundleUninstall = 0x3;
	private final int BundleUpdate = 0x4;

	public DefaultBundleServer(BundleContext context)
	{
		this.context = context;
	}

	public List<SuRenBundle> getAll()
	{
		Bundle[] bundles = context.getBundles();
		List<SuRenBundle> surenBundles = new ArrayList<SuRenBundle>();
		if(bundles == null)
		{
			return surenBundles;
		}

		for(Bundle bundle : bundles)
		{
			SuRenBundle surenBundle = new SuRenBundle();

			BundleConvert.toSuRen(bundle, surenBundle);

			surenBundles.add(surenBundle);
		}

		return surenBundles;
	}

	public List<SuRenBundle> searchBy(String match)
	{
		List<SuRenBundle> bundles = getAll();
		for(int i = 0; i < bundles.size();)
		{
			SuRenBundle bundle = bundles.get(i);

			if(!bundle.toString().contains(match))
			{
				bundles.remove(i);
			}
			else
			{
				i++;
			}
		}

		return bundles;
	}

	public List<SuRenBundle> matchBy(String regex)
	{
		return matchBy(regex, Pattern.CANON_EQ);
	}


	public List<SuRenBundle> matchBy(String regex, boolean insensitive)
	{
		if(insensitive)
		{
			return matchBy(regex, Pattern.CASE_INSENSITIVE);
		}
		else
		{
			return matchBy(regex);
		}
	}

	public List<SuRenBundle> matchBy(String regex, int flag)
	{
		List<SuRenBundle> bundles = getAll();
		Pattern pattern = null;

		try
		{
			pattern = Pattern.compile(regex, flag);
		}
		catch(IllegalArgumentException e)
		{
		}

		if(pattern == null)
		{
			return bundles;
		}

		for(int i = 0; i < bundles.size();)
		{
			SuRenBundle bundle = bundles.get(i);
			Matcher matcher = pattern.matcher(bundle.toString());
			boolean result = matcher.find();

			if(!result)
			{
				bundles.remove(i);
			}
			else
			{
				i++;
			}
		}

		return bundles;
	}

	public SuRenBundle getById(long id)
	{
		Bundle bundle = context.getBundle(id);
		if(bundle == null)
		{
			return null;
		}

		SuRenBundle surenBundle = new SuRenBundle();

		BundleConvert.toSuRen(bundle, surenBundle);

		return surenBundle;
	}

	public int start(long... ids)
	{

		return bundleOperate(BundleStart, ids);
	}

	public int stop(long... ids)
	{
		return bundleOperate(BundleStop, ids);
	}

	public int update(long... ids)
	{
		return bundleOperate(BundleUpdate, ids);
	}

	public int install(String... paths)
	{
		int count = 0;
		if(paths == null)
		{
			return count;
		}

		for(String path : paths)
		{
			try
			{
				File file = new File(path);

				if(file.isFile())
				{
					context.installBundle(file.toURI().toURL().toExternalForm());
				}

				count++;
			}
			catch (BundleException e)
			{
				e.printStackTrace();
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}

		return count;
	}

	public int uninstall(long... ids)
	{
		return bundleOperate(BundleUninstall, ids);
	}

	private int bundleOperate(int operator, long ... ids)
	{
		int count = 0;
		if(ids == null)
		{
			return count;
		}

		for(long id : ids)
		{
			Bundle bundle = context.getBundle(id);
			if(bundle == null)
			{
				continue;
			}

			try
			{
				switch(operator)
				{
					case BundleStart:
						bundle.start();
						break;
					case BundleStop:
						bundle.stop();
						break;
					case BundleUninstall:
						bundle.uninstall();
						break;
					case BundleUpdate:
						bundle.update();
						break;
				}

				count++;
			}
			catch (BundleException e)
			{
				e.printStackTrace();
			}
		}

		return count;
	}
}
