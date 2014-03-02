package org.suren.littlebird.setting;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.suren.littlebird.setting.SshSetting.Ssh;

public class ListAdapter extends XmlAdapter<SshSetting.Ssh[], List<SshSetting.Ssh>>
{

	@Override
	public List<Ssh> unmarshal(Ssh[] v) throws Exception
	{
		if(v == null)
		{
			return null;
		}
		else
		{
			List<Ssh> sshList = new ArrayList<Ssh>();
			for(Ssh ssh : v)
			{
				sshList.add(ssh);
			}
			
			return sshList;
		}
	}

	@Override
	public Ssh[] marshal(List<Ssh> v) throws Exception
	{
		if(v == null)
		{
			return null;
		}
		else
		{
			return v.toArray(new Ssh[0]);
		}
	}

}
