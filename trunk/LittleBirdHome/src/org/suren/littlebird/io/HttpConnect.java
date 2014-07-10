/**
 *
 */
package org.suren.littlebird.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author suren
 *
 */
public class HttpConnect
{
	public String request(String url)
	{
		if(url == null)
		{
			return null;
		}

		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);

		try
		{
			CloseableHttpResponse response = client.execute(httpGet);

			System.out.println(response);

			InputStream input = response.getEntity().getContent();

			byte[] buf = new byte[1024];
			int len = -1;
			StringBuffer contentBuf = new StringBuffer();

			while((len = input.read(buf)) != -1)
			{
				contentBuf.append(new String(buf, 0, len));
			}

			return contentBuf.toString();
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public String post(String url, Map<String, Object> data)
	{
		if(url == null)
		{
			return null;
		}

		String response = null;

		List<NameValuePair> formParams = new ArrayList<NameValuePair>();

		if(data != null)
		{
			for(String key : data.keySet())
			{
				formParams.add(new BasicNameValuePair(key, data.get(key).toString()));
			}
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);

		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);

		CloseableHttpClient client = HttpClients.createDefault();

		try
		{
			client.execute(httpPost);
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return response;
	}

	public static void main(String[] args)
	{
		new HttpConnect().post("www.baidu.com", null);
	}
}
