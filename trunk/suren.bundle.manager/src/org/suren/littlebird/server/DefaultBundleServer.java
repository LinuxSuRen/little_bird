package org.suren.littlebird.server;

import java.util.ArrayList;
import java.util.List;

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

	public DefaultBundleServer(BundleContext context)
	{
		this.context = context;
	}

	public String hello()
	{
		return "hello osgi";
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
				context.installBundle(path);

				count++;
			}
			catch (BundleException e)
			{
				e.printStackTrace();
			}
		}

		return 0;
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
				}

				count++;
			}
			catch (BundleException e)
			{
				e.printStackTrace();
			}
		}

		return 0;
	}
}
