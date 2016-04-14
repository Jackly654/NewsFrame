package hz.dodo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RC_WEB extends Thread
{
	public interface NetWebCallBack
	{
		public void recGetRes(InputStream is, int size, String tag1, String tag2);
		public void recGetErr(String code, String tag1, String tag2);
	}
	
	String httpurl, tag1, tag2;
	NetWebCallBack callback;
	public RC_WEB(NetWebCallBack callback, String url, String tag1, String tag2)
	{
		httpurl = url;
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.callback = callback;
		if(httpurl != null && httpurl.length() > 0) start();
	}

	public void run()
	{
		try
		{
			URL url = null;
			HttpURLConnection httpConn = null;
			InputStream is = null;

			try
			{
				url = new URL(httpurl);
				httpConn = (HttpURLConnection) url.openConnection();
				HttpURLConnection.setFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				// User-Agent : http://www.focuznet.com/siteroad/t1991/
//				httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
				httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)");

				// logger.info(httpConn.getResponseMessage());
				int rc = httpConn.getResponseCode();
				if(rc == 200)
				{
					is = httpConn.getInputStream();
					if(callback != null) callback.recGetRes(is, httpConn.getContentLength(), tag1, tag2);
				}
				else
				{
					if(callback != null) callback.recGetErr("backCode: " + rc, tag1, tag2);
				}
			}
			catch (Exception e1)
			{
				Logger.e("RC_WEB run " + e1.toString());
			}
			finally
			{
				try
				{
					is.close();
					httpConn.disconnect();
				}
				catch (Exception ex)
				{
					Logger.i("is.close " + ex.toString());
				}
			}
		}
		catch (Exception e1)
		{
//				String msg;
//				msg = e1.toString().substring(10);
//				if (msg.indexOf("FileNotFoundException") != -1) main.rec_err(httpurl, fn, "-" + msg);
			// 其它情况予以忽视（通常是域名解析失败之类的，和手机设置有关，无须客户端处理）
//				main.rec_err(httpurl, fn, "-" + msg);
			if(callback != null) callback.recGetErr(e1.toString().substring(10), tag1, tag2);
		}
	}
}
