package hz.dodo;

import java.io.File;
import java.io.InputStream;

import hz.dodo.RC_POST.NetPostCallBack;
import android.content.Context;

public class PostLog implements NetPostCallBack
{
	public interface Callback
	{
		public void rscMsg(int code, String tag);
	}
	
//	static final String url_server = "http://www.haodongdong.com/log/log?pkgname=";
//	static final String url_server = "http://58.215.178.102/dodopost/postLog?1=1";
	static final String url_server = "http://www.hododo.com.cn/dodopost/postLog?1=1";
//	static final String url_server = "http://192.168.5.236:8080/dodopost/postLog?1=1";
	static String logPath ;
	
	static FileUtil fu;
	static Callback callback;
//	static PostLog mThis;
	static Context ctx;
//	public static PostLog getInstance(Callback callback, Context ctx)
//	{
//		if(mThis == null) mThis = new PostLog(callback, ctx);
//		return mThis;
//	}
	
	public PostLog(Callback callback, Context ctx)
	{
		PostLog.callback = callback;
		PostLog.ctx = ctx;
		logPath = SDCard.getSDCardRootPath(ctx) + "/.Qeek/.log";
	}
	
	// priority:0为立即上传,1为可以先保存文件,2为上传已储存的文件
	public void post(final int priority, final String msg, final String tag)
	{
		new Thread()
		{
			public void run()
			{
				if(priority == 0)
				{
					new RC_POST(PostLog.this,
							url_server,
							SystemUtil.getSendLogBaseInfo(ctx) + "__" + msg + "&timer=" + System.currentTimeMillis()+"&table="+SystemUtil.getPkgName(ctx).replace(".","_")+
							"&vcode=" + SystemUtil.getVCode(ctx, null)+"&vname=" + SystemUtil.getVName(ctx, null),
							"" + priority,
							tag);
				}
				else if(priority == 1)
				{
					try
					{
						if(fu == null) fu = new FileUtil();
						
						String str = StrUtil.byte2hex((msg + "&timer=" + System.currentTimeMillis() +"&table="+SystemUtil.getPkgName(ctx).replace(".","_")+"&vcode=" + SystemUtil.getVCode(ctx, null)+"&vname=" + SystemUtil.getVName(ctx, null)+ "#").getBytes());
						if(str != null) // 写入文件
						{
							fu.writeAppend(str,logPath);
						}
						sendMsg(false);
					}
					catch(Exception e1)
					{
//						Logger.e("PostLog::post()=" + e1.toString());
						e1.printStackTrace();
					}
				}else
				{
					//直接上传文件
					if(fu == null) fu = new FileUtil();
					sendMsg(true);
				}
			};
		}.start();
	}
	
	public void sendMsg(boolean post)
	{
		if(!NetStatus.getNetStatus(ctx)) return;
		
		if(FileUtil.size(logPath) > FileUtil.KB || post)
		{
			String read = fu.read(logPath);
			if(read != null)
			{
				read.replace("\n", "");
				byte[] bys= StrUtil.hex2byte(StrUtil.replaceBlank(read));
				if(bys != null)
				{
					String str2 = new String(bys);
					if(str2.startsWith("#"))
					{
						str2 = str2.substring(1);
					}
					if(str2.endsWith("#"))
					{
						str2 = str2.substring(0, str2.length() - 1);
					}
					new RC_POST(PostLog.this, url_server,SystemUtil.getSendLogBaseInfo(ctx) +"__"+ str2, "1", "file");
				}
			}
		}
	}

	public void recPostRes(InputStream is, int size, String tag1, String tag2)
	{
		if("0".equals(tag1)) // 需要立即上传的
		{
			try
			{
				if(callback != null) callback.rscMsg(0, tag2);
			}
			catch(Exception e1)
			{
//				Logger.e("" + e1.toString());
				e1.printStackTrace();
			}
		}
		else
		{
			// 删除文件
			FileUtil.delete(new File(logPath));
			Logger.i("文件已上传,删除源文件");
		}
	}

	public void recPostErr(String code, String tag1, String tag2)
	{
		if("0".equals(tag1)) // 需要立即上传的
		{
			try
			{
				if(callback != null) callback.rscMsg(Integer.valueOf(code), tag2);
			}
			catch(Exception e1)
			{
//				Logger.e("" + e1.toString());
				e1.printStackTrace();
			}
		}
	}
}
