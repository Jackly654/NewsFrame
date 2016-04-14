package hz.dodo;

import hz.dodo.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

@SuppressWarnings ({ "deprecation" })
public class RC_POSTMulti extends Thread
{
	public interface CallBack
	{
		public void recPostRes(InputStream is, int size, String tag1, String tag2);
		public void recPostErr(String code, String tag1, String tag2);
	}
	
	CallBack
		callback;
	
	HashMap<String, ?>
		hm;
	
	String
		url,
		msg,
		tag1,
		tag2;
	
	public RC_POSTMulti(final CallBack callback, HashMap<String, ?> hm, final String url, final String tag1, final String tag2)
	{
		this.callback = callback;
		this.url = url;
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.hm = hm;
		if(hm != null && url != null && url.length() > 0)
		{
			start();
		}
	}

	@SuppressWarnings ("unchecked")
	public void run()
	{
		if(hm == null) return;

		HttpClient httpclient = null;
		HttpEntity resEntity = null;
		try
		{
			MultipartEntity mpEntity = null;
			Iterator<?> iter = hm.entrySet().iterator();
			
			if(iter != null)
			{
				String sKey;
				Entry<String, Object> entry;
				
				mpEntity = new MultipartEntity(); //文件传输
				while(iter.hasNext())
				{ 
					if(null != (entry = (Entry<String, Object>)iter.next()))
					{
						if(null != (sKey = entry.getKey()) && entry.getValue() != null)
						{
							if(sKey.startsWith("file_"))
							{
								mpEntity.addPart("file", new FileBody((File)(entry.getValue())));
							}
							else
							{
							    mpEntity.addPart(sKey, new StringBody(entry.getValue().toString(), Charset.forName("UTF-8")));
							}
						}
					}
				}
				
				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(mpEntity);

				httpclient = new DefaultHttpClient();
				//设置通信协议版本
				httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				
				HttpResponse response = httpclient.execute(httppost);
				
				int code = response.getStatusLine().getStatusCode();
				if(code == HttpStatus.SC_OK)
				{
					if(callback != null)
					{
						resEntity = response.getEntity();
						InputStream is = resEntity.getContent();
						callback.recPostRes(is, -1, tag1, tag2);
						is.close();
					}
				}
				else
				{
					if(callback != null) callback.recPostErr("" + code, tag1, tag2);
				}
			}
		}
		catch(Exception ext)
		{
			if(callback != null) callback.recPostErr("", tag1, tag2);
			Logger.e("RC_POSTMulti run " + ext.toString());
		}
		finally
		{
			if(httpclient != null && httpclient.getConnectionManager() != null)
			{
				try{ httpclient.getConnectionManager().shutdown(); } catch (Exception ext){}
			}
			if (resEntity != null)
			{
				try{ resEntity.consumeContent(); } catch (IOException ext){}
			}
		}
	}
}
