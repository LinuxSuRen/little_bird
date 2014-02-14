package org.suren.littlebird.net.ssh;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SimpleUserInfo implements UserInfo, UIKeyboardInteractive
{

	private Component parent;
	private String password;

	public SimpleUserInfo() {
	}

	public SimpleUserInfo(Component parent) {
		this.parent = parent;
	}

	public String getPassphrase()
	{
		System.out.println("getPassphrase=2==");
		return null;
	}

	public String getPassword()
	{
		System.out.println("getPassword=2==");
		return JOptionPane.showInputDialog(parent, "Please Input your word", "Type info",
				JOptionPane.OK_CANCEL_OPTION);
	}

	public boolean promptPassphrase(String arg0)
	{
		System.out.println(arg0 + "=2==");
		return true;
	}

	public boolean promptPassword(String arg0)
	{
		System.out.println(arg0 + "=3==");
		return true;
	}

	public boolean promptYesNo(String arg0)
	{
//		System.out.println(arg0 + "=4==");
//		return true;

		Object[] options = { "yes", "no" };
		int foo = JOptionPane.showOptionDialog(null, arg0, "Warning",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, options, options[0]);
		return foo == 0;
	}

	public void showMessage(String arg0)
	{
		System.out.println(arg0 + "=5==");
	}

	@Override
	public String[] promptKeyboardInteractive(String arg0, String arg1,
			String arg2, String[] arg3, boolean[] arg4)
	{
		System.out.println("promptKeyboardInteractive");
		
		String pwd = password;
		if(pwd == null || "".equals(pwd))
		{
			pwd = JOptionPane.showInputDialog(parent, "Please Input your word", "Type info",
					JOptionPane.OK_CANCEL_OPTION);
		}
		
		return new String[]{pwd};
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
