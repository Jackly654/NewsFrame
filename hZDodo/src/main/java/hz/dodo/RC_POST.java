package hz.dodo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RC_POST extends Thread
{
	public interface NetPostCallBack
	{
		public void recPostRes(InputStream is, int size, String tag1, String tag2);
		public void recPostErr(String code, String tag1, String tag2);
	}
	
	String rooturl, msg, tag1, tag2;
	NetPostCallBack callback;

	public RC_POST(final NetPostCallBack callback, final String url, final String msg, final String tag1, final String tag2)
	{
		rooturl = url;
		this.msg = msg;
		this.tag1= tag1;
		this.tag2 = tag2;
		this.callback = callback;
		
		if(msg != null && msg.length() > 0) start();
	}

	public void run()
	{
		int err = -1;
		HttpURLConnection ucon = null;
		try
		{
			Logger.i("Post Run");
			
			URL target = new URL(rooturl);
			err = -2;
			ucon = (HttpURLConnection) target.openConnection();
			err = -3;

			// 设定链接类型为 POST
			ucon.setRequestMethod("POST");

			// 一些可能必要的设定
			ucon.setDoOutput(true);
			ucon.setDoInput(true);
			ucon.setUseCaches(false);

			// 这个我不确定需要不需要
//			String name = URLEncoder.encode("名字","utf-8");
//			ucon.setRequestProperty("NAME", name);

			ucon.setRequestProperty("Connection", "Keep-Alive"); // 原来已经有这个
			ucon.setRequestProperty("Content-Type", "application/octet-stream");
			ucon.setRequestProperty("Charset", "UTF-8");

			err = -4;
			byte[] msg_b = msg.getBytes(/*ENCODING_UTF_8*/);
			err = -5;
			ucon.setRequestProperty("Content-length", "" + msg_b.length);
			
			Logger.i("Call getOutputStream()");
			
			// 把 POST 的内容发送出去
			err = -6;
			OutputStream outputStream = ucon.getOutputStream();
			outputStream.write(msg_b);
			outputStream.close();

			err = -7;
			Logger.i("getOutputStream() Complete, Call getResponseCode()");
			int rc = ucon.getResponseCode();
			
			/*URL target;
			target = new URL(rooturl + fn);
			System.out.println(rooturl + fn);
			HttpURLConnection ucon = (HttpURLConnection) target.openConnection();
			ucon.setRequestProperty("Connection", "Keep-Alive");
			int rc = ucon.getResponseCode();*/
			
			if (rc == 200)
			{
				Logger.i("(rc == 200), msg: " + msg);
				// int total;
				// total = ucon.getContentLength();
				
				err = -8;
				InputStream is = ucon.getInputStream();
				err = -9;
				if(callback != null) callback.recPostRes(is, ucon.getContentLength(), tag1, tag2);
				is.close();
			}
			else
			{
				Logger.d("rc == " + rc);
				err = -10;
//				main.rec_err(rooturl, msg, "-RC" + rc);
				if(callback != null) callback.recPostErr("" + rc, tag1, tag2);
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
//			Logger.e("Con Error : " + e1.toString());
//			main.rec_err(rooturl, msg, "-RC" + e1.toString());
//			if(callback != null) callback.recPostErr("-1", tag1, e1.toString());
			if(callback != null) callback.recPostErr("" + err, tag1, tag2);
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
