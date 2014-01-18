package org.suren;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author suren
 *
 */
public class Generator
{
	private final String META_PATH = "META-INF/MANIFEST.MF";
	
	private final String ALG_RSA = "RSA";

	public int metaHash(String path)
	{
		JarFile jarFile = null;
		StringBuffer buffer = new StringBuffer();
		
		try
		{
			jarFile = new JarFile(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		
		if(jarFile != null)
		{
			JarEntry entry = null;
			try
			{
				entry = jarFile.getJarEntry(META_PATH);
			}
			catch (IllegalStateException e)
			{
				e.printStackTrace();
			}
			
			InputStream entryStream = null;
			try
			{
				entryStream = jarFile.getInputStream(entry);
				
				int len = -1;
				byte[] b = new byte[1024];

				while ((len = entryStream.read(b)) != -1)
				{
					buffer.append(new String(b, 0, len));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(entryStream != null)
				{
					try
					{
						entryStream.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return buffer.toString().hashCode();
	}
	
	public void generateKeyPair()
	{
		try
		{
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALG_RSA);
			keyPairGenerator.initialize(1024);
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			PrivateKey privateKey = keyPair.getPrivate();
			Signature signature = Signature.getInstance(privateKey.getAlgorithm());
			signature.initSign(privateKey);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
	}
	
	public void certificate()
	{
		try
		{
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			
			keyStore.load(null, null);
			
			Enumeration<String> aliases = keyStore.aliases();
			while(aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
			}
		}
		catch (KeyStoreException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (CertificateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
