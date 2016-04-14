package hz.dodo;

import hz.dodo.controls.DDialog;
import hz.dodo.download.Download;

import java.io.InputStream;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Handler;
import android.os.Message;

public class UpVersion implements Handler.Callback
{
	public interface Callback
	{
		public void upVersionStatus(final int status);
	}
	// http://www.haodongdong.com/ftp.tar.gz // 200M
	// www.haodongdong.com/gamecenter.apk // 5M
	// http://www.haodongdong.com/app/C99/Qeek_weather_1_0_14.apk 11M

	public static final int S_ERROR = -1;
	public static final int S_UNCONNECT = 0; // 无网络连接
	public static final int S_UNMOUNTED = 1; // 无SD卡
	public static final int S_UNUP = 2; // 无需更新
	public static final int S_FINDED_NEW = 3; // 发现新版本
	public static final int S_BEGIN_DOWNLOAD_APK = 4; // 开始下载apk
	public static final int S_RUNNING = 5; // 正在下载中
	public static final int S_INSTALL_APK = 6; // 要安装apk
	public static final int S_DISMISS_DIALOG = 7; // 隐藏弹出框
	
	final int MSG_UP_NORMAL_TIPS = 0;
	final int MSG_UP_FORCED_TIPS = 1;
	final int MSG_INSTALL_APK = 2;
	
	final String URL_UP_ROOT = "http://www.haodongdong.com/update1.php";

	Activity at;
	Handler handler;
	DDialog dlg;
	int fw, fh,
		isForce, // 0非强制, 1强制
		newVCode; // 最新版本
	String upUrl,
		   apkUrl,
		   newVName,
		   channel,
		   locApkPath;

	Callback callback;
	public UpVersion(Activity at, int fw, int fh)
	{
		this.at = at;
		this.fw = fw;
		this.fh = fh;
		channel = "-1";
		handler = new Handler(this);
	}
	public void update(Callback callback)
	{
		this.callback = callback;
		checkNewVersion();
	}

	// 获取新版本信息
	private void checkNewVersion()
	{
		try
		{
			if (NetStatus.getNetStatus(at))
			{
				if(!SDCard.checkSdcard())
				{
					Logger.d("无SD卡");
					toCallback(S_UNMOUNTED);
					return;
				}

				String pkg = at.getPackageName();
				String curv = SystemUtil.getVCode(at, pkg);
				channel = PkgMng.getApplicationMetaData(at, pkg, "CHANNEL");
				upUrl = URL_UP_ROOT + "?v_code=" + curv + "&pkg=" + pkg + "&channel=" + channel;
//				upUrl = "http://www.haodongdong.com/update1.php?v_code=1&pkg=com.dodo.launcher&channel=C00";

//				long id = SPUtil.getLong(at, "SP_TABLE", upUrl, -1);
//				if(id > -1)
//				{
//					int status = Download.queryDownloadStatus(at, id);
//					if(status == DownloadManager.STATUS_RUNNING)
//					{
//						Logger.d("已经在下载中,不必重复执行");
//						toCallback(S_RUNNING);
//						return;
//					}
//				}

				Logger.i("获取是否有新版本可供更新");
				new RC_GET(new RC_GET.NetGetCallBack()
				{
					public void recGetRes(InputStream is, int size, String tag1, String tag2)
					{
						Logger.i("新版本可供更新信息正常返回");
						// 1.解析字符串
						byte[] btarray = StrUtil.getByte(is);
						if (btarray != null)
						{
							try
							{
								String ts1 = new String(btarray, "UTF-8");
								ts1 = ts1.trim();

								if (ts1.charAt(0) == 65279)
								{
									ts1 = ts1.substring(1);
								}

								ts1 = ts1.replaceAll("\\s", ""); // 去掉所有不可见字符
								decodeRC_GET(ts1);
							}
							catch(Exception ect)
							{
								Logger.e("返回升级信息 recGetRes()" + ect.toString());
							}
						}
						else
						{
							Logger.d("返回信息有误");
							toCallback(S_ERROR);
						}
					}

					public void recGetErr(String code, String tag1, String tag2)
					{
						Logger.d("获取新版本信息失败 code:" + code);
						toCallback(S_ERROR);
					}
				}, upUrl, "", "");
			}
			else
			{
				Logger.d("无网络");
				toCallback(S_UNCONNECT);
			}
		}
		catch (Exception ect)
		{
			toCallback(S_ERROR);
			Logger.e("checkNewVersion() " + ect.toString());
		}
	}
	
	private void decodeRC_GET(final String cnt)
	{
		if(cnt == null)
		{
			Logger.d("无返回内容");
			return;
		}

		if ("NA".equals(cnt))
		{
			// 不需要更新
			Logger.i("不需要更新");
			toCallback(S_UNUP);
		}
		else
		{
			String[] strs = cnt.split("&");

			String log = "",
				   v_code = "0";

			int i1 = 0;
			while (i1 < strs.length)
			{
				if (strs[i1].startsWith("updatelog"))
				{
					log = strs[i1].substring(strs[i1].indexOf("=") + 1);
				}
				else if (strs[i1].startsWith("url"))
				{
					apkUrl = strs[i1].substring(strs[i1].indexOf("=") + 1);
				}
				else if (strs[i1].startsWith("v_code"))
				{
					v_code = strs[i1].substring(strs[i1].indexOf("=") + 1);
				}
				else if (strs[i1].startsWith("v_name"))
				{
					newVName = strs[i1].substring(strs[i1].indexOf("=") + 1);
				}
				else if (strs[i1].startsWith("isforce"))
				{
					isForce = Integer.parseInt(strs[i1].substring(strs[i1].indexOf("=") + 1));
				}
				i1++;
			}
			
			Logger.i("log:" + log);
			Logger.i("apkUrl:" + apkUrl);
			Logger.i("v_code:" + v_code);
			Logger.i("newVName:" + newVName);
			Logger.i("isForce:" + isForce);

			// 2.获取当前版本进行对比
			try
			{
				String code = SystemUtil.getVCode(at, SystemUtil.getPkgName(at));
				int curVcode = Integer.valueOf(code);
				newVCode = Integer.valueOf(v_code);
				
				if(newVCode > curVcode)
				{
					Logger.i("发现新版本");
					toCallback(S_FINDED_NEW);
					Message msg = handler.obtainMessage();
					msg.obj = log;
					
					if(isForce == 0)
					{
						msg.what = MSG_UP_NORMAL_TIPS;
					}
					else
					{
						msg.what = MSG_UP_FORCED_TIPS;
					}
					handler.sendMessage(msg);
				}
				else
				{
					Logger.i("不必更新");
					toCallback(S_UNUP);
				}
			}
			catch (Exception e1)
			{
				Logger.e("检测失败" + e1.toString());
				toCallback(S_ERROR);
			}
		}
	}
	
	DDialog getDlg()
	{
		if(dlg == null) dlg = new DDialog(at, fw, fh);
		return dlg;
	}

	public boolean handleMessage(Message msg)
	{
		switch(msg.what)
		{
			case MSG_UP_NORMAL_TIPS: // 非强制升级
			case MSG_UP_FORCED_TIPS: // 强制升级
				showUpTips(msg.what, (String)msg.obj);
				break;
			case MSG_INSTALL_APK:
				showUpTips(msg.what, "确定要安装吗?");
				break;
		}
		return true;
	}
	
	private void showUpTips(final int type, final String log)
	{
		DDialog dialog = getDlg();
		String tag = "";
		switch(type)
		{
			case MSG_UP_NORMAL_TIPS: // 非强制升级
				dialog.setTitle("升级提示");
				dialog.setContent(log, ";");
				
				tag = "MSG_UP_NORMAL_TIPS";
				break;
			case MSG_UP_FORCED_TIPS: // 强制升级
				dialog.setTitle("升级提示");
				dialog.setContent(log, ";");
				dialog.setBtn("退出", "确定");
				
				tag = "MSG_UP_FORCED_TIPS";
				break;
			case MSG_INSTALL_APK:
				dialog.setTitle("安装提示");
				dialog.setContent("确定安装吗?");
				
				tag = "MSG_INSTALL_APK";
				break;
		}
		
		dialog.show(new DDialog.Callback()
		{
			public void onClick(int type, String tag)
			{
				toCallback(S_DISMISS_DIALOG);
				if("MSG_UP_FORCED_TIPS".equals("" + tag)) // 强制升级
				{
					switch(type)
					{
						case DDialog.CLICK_KEYBACK:
//							if(at != null) at.finish();
//							break;
						case DDialog.CLICK_LEFT:
//							{
//								if(checkApk())
//								{
//									Logger.i("发现强制升级本地安装包,调起安装");
//									PkgMng.install(at, locApkPath);
//								}
//								if(at != null) at.finish();
//							}
							if(at != null) at.finish();
							break;
						case DDialog.CLICK_RIGHT:
							Logger.i("要下载APK");
							if(!checkApk())
							{
								if(at != null) at.finish();
							}
							break;
					}
				}
				else if("MSG_UP_NORMAL_TIPS".equals("" + tag)) // 非强制升级
				{
					switch(type)
					{
						case DDialog.CLICK_RIGHT:
							Logger.i("要下载APK");
							checkApk();
							break;
					}
				}
				else if("MSG_INSTALL_APK".equals("" + tag))
				{
					switch(type)
					{
						case DDialog.CLICK_KEYBACK:
						case DDialog.CLICK_LEFT:
							break;
						case DDialog.CLICK_RIGHT:
							Logger.i("安装APK");
							if(locApkPath != null)
							{
								PkgMng.install(at, locApkPath);
							}

							break;
					}
					
					if(isForce == 1)
					{
						if(at != null) at.finish();
					}
				}
			}
		}, "" + tag);
	}
	
	// 返回值,是否本地存在apk
	private boolean checkApk()
	{
		// 3.检查tag2路径下是否存在apk,并检查版本
		if(!SDCard.checkSdcard())
		{
			Logger.i("本地没有SD卡");
			toCallback(S_UNMOUNTED);
			return false;
		}

		int status = -1;
		long locId = SPUtil.getLong(at, "SP_TABLE", upUrl, -1);
		String locPath = null;
		if(locId >= 0)
		{
			status = Download.queryDownloadStatus(at, locId);
			locPath = Download.queryDownloadedPath(at, locId);
			Logger.i("本地apk地址:" + locPath);
		}
		
		if(locPath != null && FileUtil.isExists(locPath) != null && status == DownloadManager.STATUS_SUCCESSFUL)
		{
			Logger.i("本地发现apk");
			int locVCode = PkgMng.getVCodeByPath(at, locPath);
			String locPkg = PkgMng.getPkgNameByPath(at, locPath);
			String locChannel = PkgMng.getApplicationMetaDataApk(at, locPath, "CHANNEL");
			String pkg = at.getPackageName();
			if(("" + pkg).equals("" + locPkg) && ("" + channel).equals("" + locChannel) && locVCode == newVCode)
			{
//				if(isForce == 0) // 非强制升级
				{
					locApkPath = locPath;
					Message msg = handler.obtainMessage();
					msg.what = MSG_INSTALL_APK;
					handler.sendMessage(msg);
				}

				Logger.i("该apk即服务器中的apk,可以直接安装");
				
				toCallback(S_INSTALL_APK);
				return true;
			}
		}
		
		if(status == DownloadManager.STATUS_RUNNING)
		{
			Logger.d("已经在下载中,不必重复执行");
			toCallback(S_RUNNING);
			return false;
		}
		
		String pkg = at.getPackageName();
		Download download = new Download(at);
		long id = download.download(apkUrl, "/apks/", PkgMng.getAppName(at, pkg), "版本号:v" + newVName, pkg + ".apk");
		Logger.i("执行下载apk任务, id:" + id);
		SPUtil.saveLong(at, "SP_TABLE", upUrl, id);
		
		toCallback(S_BEGIN_DOWNLOAD_APK);
		return false;
	}
	
	private void toCallback(final int status)
	{
		if(callback != null) callback.upVersionStatus(status);
	}
}