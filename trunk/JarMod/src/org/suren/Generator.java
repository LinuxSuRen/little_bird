
package org.suren;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.binary.Base64;

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
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(jarFile != null)
			{
				try
				{
					jarFile.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return buffer.toString().hashCode();
	}
	
	public static void main(String[] args) throws Exception
	{
		Generator generator = new Generator();
		
		generator.generateKeyPair();
	}
	
	public void generateKeyPair() throws Exception
	{
		try
		{
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALG_RSA);
			keyPairGenerator.initialize(1024);
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			PrivateKey privateKey = keyPair.getPrivate();
			Signature signature = Signature.getInstance("SHA1WithRSA");
			signature.initSign(privateKey);
			
			FileInputStream inStream = new FileInputStream(new File("C:/suren/iKVM32.dll"));
			
			byte[] buf = new byte[1024];
			int len = -1;
			while((len = inStream.read(buf)) != -1)
			{
				signature.update(buf, 0, len);
			}
			inStream.close();
			
			byte[] signArray = signature.sign();
			
			Base64 base64 = new Base64();
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
			messageDigest.update(signArray);
			signArray = messageDigest.digest();
			
			for(byte sign : base64.encode(signArray))
			{
				System.out.print((char)(sign & 0xff));
			}
			System.out.println();
			
			byte[] baseArray = base64.encode(signArray);
			for(byte base :baseArray)
			{
				System.out.print((char)base);
			}
			System.out.println();
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("c:/suren/a.dat")));
			out.write(privateKey.getEncoded());
			out.close();
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
