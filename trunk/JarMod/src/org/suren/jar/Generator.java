package org.suren.jar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

/**
 * @author suren
 *
 */
public class Generator
{
	private final String ALG_RSA = "RSA";

	@SuppressWarnings("resource")
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
				entry = jarFile.getJarEntry(JarFile.MANIFEST_NAME);
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
	
	public void certificate() throws Exception
	{
		try
		{
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			
			keyStore.load(null, null);
			
			Enumeration<String> aliases = keyStore.aliases();
			while(aliases.hasMoreElements())
			{
//				String alias = aliases.nextElement();
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
	
	public static void main(String[] args) throws Exception
	{
		KeyStore keyStore = KeyStore.getInstance("JKS");
		System.out.println(keyStore);
		
		FileInputStream inStream = new FileInputStream(new File("d:/key"));
		
		keyStore.load(inStream, "walkman".toCharArray());
		
		Enumeration<String> aliases = keyStore.aliases();
		if(aliases != null)
		{
			while(aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				System.out.println(alias);
				
				Key key = keyStore.getKey(alias, "walkman".toCharArray());
				
				if(key != null)
				{
					System.out.println(key.getAlgorithm());
					System.out.println(key.getFormat());
					System.out.println(Arrays.toString(key.getEncoded()));
				}
				
//				Certificate certificate = keyStore.getCertificate(alias);
				Certificate[] certChain = keyStore.getCertificateChain(alias);
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				X509Certificate[] xCertChain = new X509Certificate[certChain.length];
				for(int i = 0; i < certChain.length; i++)
				{
					ByteArrayInputStream certIn = new ByteArrayInputStream(certChain[i].getEncoded());
					X509Certificate cert = (X509Certificate) certFactory.generateCertificate(certIn);
					xCertChain[i] = cert;
				}
				
				KeyFactory keyFactory = KeyFactory.getInstance(key.getAlgorithm());
				RSAPrivateKeySpec keySpec = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
				PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
				
				System.out.println(xCertChain[0].getEncoded().length);
				
				FileOutputStream fileOut = new FileOutputStream(new File("e:/check/123"));
				fileOut.write(xCertChain[0].getEncoded());
				fileOut.close();
				
				PublicKey publicKey = xCertChain[0].getPublicKey();
				System.out.println("public key lenght : " + publicKey.getEncoded().length);
				
				String targetStr = "77tzSQuuqvaiygtY0sJr0rM/deU=";
				
				Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
				cipher.init(Cipher.ENCRYPT_MODE, privateKey);
				byte[] enResult = cipher.doFinal(targetStr.getBytes());
				
				MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
				messageDigest.update(enResult);
				Base64 base64 = new Base64();
				base64.encode(messageDigest.digest());
				
				fileOut = new FileOutputStream(new File("e:/check/124"));
				fileOut.write(base64.encode(messageDigest.digest()));
				fileOut.close();
				
				System.out.println("private Key length : " + privateKey.getEncoded().length);
				
				cipher.init(Cipher.DECRYPT_MODE, publicKey);
				byte[] result = cipher.doFinal(enResult);
				System.out.println(new String(result));
			}
		}
		
		inStream.close();
	}

}
