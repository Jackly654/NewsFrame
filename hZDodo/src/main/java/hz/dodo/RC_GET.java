package hz.dodo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RC_GET extends Thread
{
	public interface NetGetCallBack
	{
		public void recGetRes(InputStream is, int size, String tag1, String tag2);
		public void recGetErr(String code, String tag1, String tag2);
	}
	
	String httpurl, tag1, tag2;
	NetGetCallBack callback;
	public RC_GET(NetGetCallBack callback, String url, String tag1, String tag2)
	{
		httpurl = url;
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.callback = callback;
		if(httpurl != null && httpurl.length() > 0) start();
	}

	public void run()
	{
		HttpURLConnection ucon = null;
		try
		{
			URL target;
			target = new URL(httpurl);
			ucon = (HttpURLConnection) target.openConnection();
			ucon.setRequestProperty("Connection", "Keep-Alive");
			// 断点续传 !!! if add Randge字段后,不是返回200,而是返回206
//			ucon.addRequestProperty("Range", "bytes=1024-2048"); // 从1024到2048
//			ucon.addRequestProperty("Range", "bytes=1024-"); // 从1024开始到文件尾
			int rc = ucon.getResponseCode();
			if (rc == 200)
			{
				InputStream is = ucon.getInputStream();
				if(callback != null) callback.recGetRes(is, ucon.getContentLength(), tag1, tag2); // ucon.getContentLength():字节数
				is.close();
			}
			else
			{
				if(callback != null) callback.recGetErr("backCode: " + rc, tag1, tag2);
			}
		}
		catch (Exception e1)
		{
//			String msg;
//			msg = e1.toString().substring(10);
//			if (msg.indexOf("FileNotFoundException") != -1) main.rec_err(httpurl, fn, "-" + msg);
			// 其它情况予以忽视（通常是域名解析失败之类的，和手机设置有关，无须客户端处理）
//			main.rec_err(httpurl, fn, "-" + msg);
			if(callback != null) callback.recGetErr(e1.toString().substring(10), tag1, tag2);
		}
		finally
		{
			if(ucon != null)
			{
				try{ ucon.disconnect(); } catch(Exception ext){}
			}
		}
	}
}
