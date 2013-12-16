package org.suren.littlebird.test;

import java.io.File;

public class DiskTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		for(File file : File.listRoots())
		{
			System.out.println(file.getAbsolutePath() + "; free : " + file.getFreeSpace());
		}
	}

}
