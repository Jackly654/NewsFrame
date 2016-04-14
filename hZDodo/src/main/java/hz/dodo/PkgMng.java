package hz.dodo;

import hz.dodo.data.Empty;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.KeyEvent;

public class PkgMng
{
	// 检查某服务是否在运行
    public static boolean isRunningService(Context ctx, String serviceName)
    {
    	try
    	{
    		if(ctx == null || serviceName == null) return false;
        	
        	String pkgname = ctx.getPackageName();
        	if(pkgname == null) return false;
        	
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningServiceInfo> services = am.getRunningServices(1000);
            ComponentName cn;
            
            int i1 = 0;
            while(i1 < services.size())
            {
            	try
            	{
                	cn = services.get(i1).service;
//                	Logger.i("服务" + i1 + "(" + cn.getClassName() + "),进程(" + services.get(i1).process + ")");
                    if(pkgname.equals(cn.getPackageName()) && cn.getClassName().equals(serviceName))
                    {
                    	Logger.i("服务(" + serviceName + "),进程(" + services.get(i1).process + ")");
                        return true;
                    }
            	}
            	catch(Exception e1)
            	{
            		Logger.e("isRunningService=" + e1.toString());
            	}
                i1++;
            }
    	}
    	catch(Exception e1)
    	{
    		Logger.e("isRunningService()=" + e1.toString());
    	}
    	Logger.d("未查询到该服务(" + serviceName + ")");
        return false;
    }
    
    public static boolean isRunningService(Context ctx, final String pkgname, final String serviceName)
    {
    	try
    	{
    		if(ctx == null || serviceName == null || pkgname == null) return false;
        	
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningServiceInfo> services = am.getRunningServices(1000);
            ComponentName cn;
            
            int i1 = 0;
            while(i1 < services.size())
            {
            	try
            	{
                	cn = services.get(i1).service;
//                	Logger.i("服务" + i1 + "(" + cn.getClassName() + "),进程(" + services.get(i1).process + ")");
                    if(pkgname.equals(cn.getPackageName()) && cn.getClassName().equals(serviceName))
                    {
//                    	Logger.i("服务(" + serviceName + ")存在,进程(" + services.get(i1).process + ")");
                    	Logger.i("服务(" + serviceName + ") started:" + services.get(i1).started + ", restarting:" + services.get(i1).restarting);
                        return true;
                    }
            	}
            	catch(Exception e1)
            	{
            		Logger.e("isRunningService=" + e1.toString());
            	}
                i1++;
            }
    	}
    	catch(Exception e1)
    	{
    		Logger.e("isRunningService()=" + e1.toString());
    	}
    	Logger.d("未查询到该服务(" + serviceName + ")");
        return false;
    }
    
    // classname : WIDGET类名 如:WidgetProvider
	public static int[] getWidgetIDS(Context ctx, final String pkgName, final String className)
	{
		if(pkgName == null || className == null) return null;
		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(ctx);
		List<AppWidgetProviderInfo> list = mAppWidgetManager.getInstalledProviders();// 此处是手机上安装的所有widget注：它只是安装好了的，但并不是你放在桌面上的
		for (AppWidgetProviderInfo appWidgetProviderInfo : list)
		{
//			if (appWidgetProviderInfo.provider.getClassName().contains("System"))// 此处为你放置在桌面上的widget如果不放上去id.length为0;
//			{
//				int[] id = mAppWidgetManager.getAppWidgetIds(appWidgetProviderInfo.provider);
//			}
			
			String tmp = appWidgetProviderInfo.provider.getClassName();
			if(tmp.equals(pkgName + "." + className))
			{
				mAppWidgetManager = AppWidgetManager.getInstance(ctx);
				ComponentName mComponentName = new ComponentName(ctx, pkgName + "." + className);
				int[] ids = mAppWidgetManager.getAppWidgetIds(mComponentName);
				if(ids != null && ids.length > 0)
				{
					return ids;
				}
				else
				{
					Logger.i("没找到widget IDS");
				}
			}
//			else
//			{
//				Logger.d("widget : " + tmp);
//			}
		}
		return null;
	}

	//通过包名获取版本号
	public static int getVersionCode(final String pkgname, Context ctx)
	{
		try
		{
            if(ctx == null || pkgname == null || "".equals(pkgname)) return 0;
			PackageInfo info;
			info = ctx.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES);
			if (info != null)
			{
				return info.versionCode;
			}
		}
		catch (NameNotFoundException e)
		{
			Logger.e("getVersionCode()=" + e.toString());
		}
		return 0;
	}
    
    // filter:0(all),1(system),2(user)
	public static List<String> getInstalledPkgName(Context ctx, int filter)
	{
		try
		{
			PackageManager pkgMng = ctx.getPackageManager();
			List<PackageInfo> packs = pkgMng.getInstalledPackages(0);

			List<String> list_pkgname = new ArrayList<String>();
			ApplicationInfo appInfo;
			for (PackageInfo info : packs)
			{
				if (null != (appInfo = info.applicationInfo))
				{
					switch(filter)
					{
						case 0:
							list_pkgname.add(info.applicationInfo.packageName);
							break;
						case 1:
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
							{
								// 系统程序
								list_pkgname.add(info.applicationInfo.packageName);
							}
							break;
						case 2:
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
							{
								// 系统程序
							}
							else
							{
								// 不是系统程序
								list_pkgname.add(info.applicationInfo.packageName);
							}
							break;
					}
				}
			}
			return list_pkgname;
		}
		catch (Exception e1)
		{
			Logger.i("getInstalledPkgName()=" + e1.toString());
		}
		return null;
	}
	
	// 通过apk安装包获取 包名
	public static String getPkgNameByPath(Context ctx, final String apkpath)
	{
		try
		{
			if (FileUtil.isExists(apkpath) == null) return null;
			PackageManager pm = ctx.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkpath, PackageManager.GET_ACTIVITIES);
			if (info != null) return info.applicationInfo.packageName;
		}
		catch(Exception e1)
		{
			Logger.e("getPkgNameByPath() " + e1.toString());
		}
		return null;
	}
	// 通过路径获取版本号
	public static int getVCodeByPath(Context ctx, final String apkpath)
	{
		try
		{
			if (FileUtil.isExists(apkpath) == null) return -1;
			PackageManager pm = ctx.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkpath, PackageManager.GET_ACTIVITIES);
			if (info != null) return info.versionCode;
		}
		catch(Exception e1)
		{
			Logger.e("getVCodeByPath() " + e1.toString());
		}
		return -1;
	}
	public static String getVNameByPath(Context ctx, final String apkpath)
	{
		try
		{
			if (FileUtil.isExists(apkpath) == null) return null;
			PackageManager pm = ctx.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkpath, PackageManager.GET_ACTIVITIES);
			if (info != null) return info.versionName;
		}
		catch(Exception e1)
		{
			Logger.e("getVNameByPath() " + e1.toString());
		}
		return null;
	}
	// 获取本程序名称Label
	public static String getAppName(Context ctx, final String pkg)
	{
		try
		{
			PackageManager pm = ctx.getPackageManager();
			ApplicationInfo appInfo = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
			return pm.getApplicationLabel(appInfo).toString();
		}
		catch(Exception ect)
		{
			Logger.e("getAppName() " + ect.toString());
		}
		
		return null;
	}
	
	public static String getApplicationMetaData(Context ctx, final String pkgName, final String keyName)
	{
		try
		{
			if(ctx == null || pkgName == null || keyName == null) return null;

			ApplicationInfo appi = ctx.getPackageManager().getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
			Bundle bundle = appi.metaData;
			if (bundle != null)
			{
				return bundle.getString(keyName);
			}
		}
		catch (Exception e1)
		{
			Logger.e("getApplicationMetaData() " + e1.toString());
		}
		return null;
	}
	
	// cls:activity的名称,字符串即可
	public static String getActivityMetaData(Context ctx, final String pkgName, final String cls, final String keyName)
	{
		try
		{
			if(ctx == null || pkgName == null || keyName == null) return null;

			ActivityInfo ai = ctx.getPackageManager().getActivityInfo(new ComponentName(pkgName, pkgName + "." + cls), PackageManager.GET_META_DATA);
			if(ai != null)
			{
				Bundle bd = ai.metaData;
				if(bd != null)
				{
					return bd.getString(keyName);
				}
			}
		}
		catch (Exception e1)
		{
			Logger.e("getActivityMetaData() " + e1.toString());
		}
		return null;
	}
	
	public static String getApplicationMetaDataApk(Context ctx, final String apkpath, final String keyName)
	{
		try
		{
			if (FileUtil.isExists(apkpath) == null) return null;
			PackageManager pm = ctx.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkpath, PackageManager.GET_META_DATA);
			if(info != null)
			{
				ApplicationInfo appi = info.applicationInfo;
				if(appi != null)
				{
					Bundle bundle = appi.metaData;
					if (bundle != null)
					{
						return bundle.getString(keyName);
					}
				}
			}
		}
		catch(Exception e1)
		{
			Logger.e("getMetaData by apkpath " + e1.toString());
		}
		return null;
	}

	// 启动应用程序详情
	@SuppressLint("InlinedApi")
	public static void startAppDetails(final Context ctx, final String pkgname)
	{
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9)
		{ // 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", pkgname, null);
			intent.setData(uri);
		}
		else
		{ // 2.3以下，使用非公开的接口(查看InstalledAppDetails)
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, pkgname);
		}
		ctx.startActivity(intent);
	}
    
	// 查看程序是否安装
	static public boolean isInstalled(Context ctx, final String pkgname)
	{
		try
		{
			if(pkgname == null || pkgname.length() <= 0 || ctx == null) return false;
			ctx.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES);
			return true;
		}
		catch(Exception e1)
		{
			Logger.d("isInstalled()=" + e1.toString());
		}
		return false;
	}
	
	// 安装程序
	public static int install(Context ctx, final String path)
	{
		try
		{
			if (path == null || path.length() <= 0) return FileUtil.rst_failed;

			File file = FileUtil.isExists(path);
			if (file == null) return FileUtil.rst_failed;

			Intent it = new Intent(android.content.Intent.ACTION_VIEW);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			ctx.startActivity(it);

			return FileUtil.rst_success;
		}
		catch (Exception e1)
		{
			Logger.e("install() " + e1.toString());
		}

		return FileUtil.rst_failed;
	}

	public static int uninstall(Context ctx, final String pkgName)
	{
		try
		{
			if (pkgName == null || pkgName.length() <= 0) return FileUtil.rst_failed;
			Uri packageURI = Uri.parse("package:" + pkgName);  
			Intent it = new Intent(Intent.ACTION_DELETE, packageURI);  
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ctx.startActivity(it); // 正常卸载程序 
			return FileUtil.rst_success;
		}
		catch(Exception e1)
		{
			Logger.e("uninstall() " + e1.toString());
		}
		return FileUtil.rst_failed;
	}
	public static boolean requestRoot()
	{
	    boolean result = false;
	    OutputStream os = null;
	    DataOutputStream dos = null;
	    try
	    {
	    	Process process = Runtime.getRuntime().exec("su");
	    	os = process.getOutputStream();
	    	dos = new DataOutputStream(os);
        	dos.writeBytes("exit \n");
	        // 提交命令  
	        dos.flush();

	        // 关闭流(必须要关闭,否则process.waitFor()无响应)
	    	try
	    	{
    	        dos.close();
    	        dos = null;
	    	}catch(Exception ext){}
	    	try
	    	{
    	        os.close();
    	        os = null;
	    	}catch(Exception ext){}
	        
	        // 等待结果
	        switch(process.waitFor())
	        {
	        	case 0: // 成功
	        		result = true;
	        		break;
	        	case 1: // 失败
	        		result = false;
	        		break;
        		default: // 未知
	        		result = false;
	        		break;
	        }
	        
	        return result;
	    }
	    catch (Exception ext)
	    {
	    	Logger.e("requestRoot() " + ext.toString());
	    }
	    return false;
	}
	// 静默安装(需手机root)
//	public static boolean suInstall(final String sAbsPath)
//	{
//		if(sAbsPath == null || FileUtil.isExists(sAbsPath) == null) return false;
//		return appMng2(sAbsPath, true);
//	}
	// 静默卸载(需手机root)
//	public static boolean suUninstall(final String sPkgName)
//	{
//		if(sPkgName == null) return false;
//		return appMng2(sPkgName, false);
//	}
	/*@SuppressWarnings ("unused")
	private static boolean appMng(final String sStr, final boolean install)
	{
	    boolean result = false;
	    OutputStream os = null;
	    DataOutputStream dos = null;
	    try
	    {
	    	Process process = Runtime.getRuntime().exec("su");
	    	os = process.getOutputStream();
	    	dos = new DataOutputStream(os);
	        
	        if(install)
	        {
	        	// 安装 sStr为apk的绝对路径
	        	dos.writeBytes("chmod 777 " + sStr + "\n");
	        	dos.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + sStr);
	        }
	        else
	        {
	        	// 卸载 sStr为包名
	        	dos.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall " + sStr);
	        }
	        dos.writeBytes("exit \n");

	        // 提交命令  
	        dos.flush();

	        // 关闭流(必须要关闭,否则process.waitFor()无响应)
	    	try
	    	{
    	        dos.close();
    	        dos = null;
	    	}catch(Exception ext){}
	    	try
	    	{
    	        os.close();
    	        os = null;
	    	}catch(Exception ext){}
	        
	        // 等待结果
	        switch(process.waitFor())
	        {
	        	case 0: // 成功
	        		result = true;
	        		break;
	        	case 1: // 失败
	        		result = false;
	        		break;
        		default: // 未知
	        		result = false;
	        		break;
	        }
	    }
	    catch (Exception ext)
	    {
	    	Logger.e((install ? "install" : "uninstall") + " " + sStr + " " + ext.toString());
	    }
	    return result;
	}
	private static boolean appMng2(final String sStr, final boolean install)
	{
		boolean result = false;
		
		PrintWriter pw = null;
		Process process = null;
		try
		{
			process = Runtime.getRuntime().exec("su");
			pw = new PrintWriter(process.getOutputStream());
			pw.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
			if(install)
			{
				pw.println("pm install -r " + sStr);
			}
			else
			{
				pw.println("pm uninstall " + sStr);
			}
			pw.flush();
    		pw.close();
			
	        // 等待结果
	        switch(process.waitFor())
	        {
	        	case 0: // 成功
	        		result = true;
	        		break;
	        	case 1: // 失败
	        		result = false;
	        		break;
        		default: // 未知
	        		result = false;
	        		break;
	        }
		}
		catch (Exception ext){}
		finally
		{
			if (process != null)
			{
				process.destroy();
			}
		}
		return result;
	}*/
	public static void startApp(Context ctx, final String pkgname)
	{
		try
		{
			ctx.getPackageManager().getLaunchIntentForPackage("" + pkgname);
		}
		catch(Exception e1)
		{
			Logger.i("PkgMng startApp() " + e1.toString());
		}
	}
	
	// 启动系统日历
	static public boolean startCalendar(Context ctx)
	{
		try
		{
			if(ctx == null) return false;
			
			Intent it = new Intent();
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ComponentName cn = null;
			
			if (Build.VERSION.SDK_INT >= 8)
			{
				cn = new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity");
			}
			else
			{
				cn = new ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity");
			}
			it.setComponent(cn);
			ctx.startActivity(it);
			return true;
		}
		catch (Exception e1)
		{
			Logger.e("startCalendar()=" + e1.toString());
		}
		return false;
	}
	
	// 启动短信界面
	static public boolean startSMS(Context ctx)
	{
		try
		{
			if(ctx == null) return false;
			
			Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_DEFAULT);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	        intent.setType("vnd.android-dir/mms-sms");
	        // 或改成亦可
	        // intent.setType("vnd.android.cursor.dir/mms");
	        ctx.startActivity(intent);
	        return true;
		}
		catch(Exception e1)
		{
			Logger.e("startSMS()=" + e1.toString());
		}
		return false;
	}
	
	// 启动通话记录界面
	static public boolean startTel(Context ctx)
	{
		try
		{
			if(ctx == null) return false;
			
			if(isInstalled(ctx, "com.android.dialer"))
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setClassName("com.android.dialer", "com.android.dialer.DialtactsActivity");
				ctx.startActivity(intent);
			}
			else if(isInstalled(ctx, "com.google.android.dialer"))
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setClassName("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity");
				ctx.startActivity(intent);
			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_CALL_BUTTON);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				ctx.startActivity(intent);
			}
			return true;
		}
		catch(Exception e1)
		{
			Logger.e("startTel()" + e1.toString());
		}
		return false;
	}
	
	// 启动卸载界面
	static public void startUninstall(Activity at, final String pkgname, final int requestCode)
	{
		try
		{
			if(at == null || pkgname == null || pkgname.length() <= 0) return;
		    Intent it = new Intent(Intent.ACTION_DELETE);
		    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		    it.setData(Uri.parse("package:" + pkgname));
		    at.startActivityForResult(it, requestCode);
		}
		catch(Exception e1)
		{
			Logger.e("uninstall()" + e1.toString());
		}
	}
	
	// 启动拨号键盘
	static public void startDial(Activity at, final String phone)
	{
		try
		{
			if(at == null || phone == null || phone.length() <= 0) return;
			Intent it = new Intent(Intent.ACTION_DIAL); // android.intent.action.DIAL
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setData(Uri.parse("tel:" + phone));
			at.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startDial() " + e1.toString());
		}
	}
	
	// 启动拨打电话
	static public void startCall(Activity at, final String phone)
	{
		try
		{
			if(at == null || phone == null || phone.length() <= 0) return;
			Intent it = new Intent(Intent.ACTION_CALL); // TODO:
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setData(Uri.parse("tel:" + phone));
			at.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startCall() " + e1.toString());
		}
	}
	
	// 启动播放视频
	static public void startVideo(Activity at, final String abspath)
	{
		try
		{
			if(at == null || abspath == null || abspath.length() <= 0) return;
			Intent it = new Intent(Intent.ACTION_VIEW); 
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//			Uri uri = Uri.parse("file:///sdcard/media.mp4");
			Uri uri = Uri.parse("file://" + abspath);
			it.setDataAndType(uri, "video/*"); 
			at.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startVideo() " + e1.toString());
		}
	}
	
	// 跳转到发送信息界面
	static public void startSMS(Activity at, final String smsto, final String body)
	{
		try
		{
			if(at == null || smsto == null || smsto.length() <= 0) return;
			
//			Uri uri = Uri.parse("smsto:13200100001");
			Uri uri = Uri.parse("smsto:" + smsto);
			Intent it = new Intent(Intent.ACTION_SENDTO, uri);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.putExtra("sms_body", "" + body);
			at.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("sendSMS() " + e1.toString());
		}
	}
	
	// 接调用短信接口发短信 <uses-permission android:name="android.permission.SEND_SMS" />
//	-- destinationAddress：目标电话号码 
//	-- scAddress：短信中心号码，测试可以不填 
//	-- text: 短信内容 
//	-- sentIntent：发送 -->中国移动 --> 中国移动发送失败 --> 返回发送成功或失败信号 --> 后续处理   即，这个意图包装了短信发送状态的信息 
//	-- deliveryIntent： 发送 -->中国移动 --> 中国移动发送成功 --> 返回对方是否收到这个信息 --> 后续处理  即：这个意图包装了短信是否被对方收到的状态信息（供应商已经发送成功，但是对方没有收到）。
	static public void sendSMS(Context ctx, final String phone, String body)
	{
		// 监听发送状态
		String SENT_SMS_ACTION = "SENT_SMS_ACTION";
		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SENT_SMS_ACTION), 0);
		ctx.registerReceiver(new BroadcastReceiver()
		{
			public void onReceive(Context _ctx, Intent _it)
			{
				switch (getResultCode())
				{
					case Activity.RESULT_OK:
						Logger.i("消息发送成功");
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					case SmsManager.RESULT_ERROR_RADIO_OFF:
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Logger.i("消息发送失败");
						break;
				}
			}
		}, new IntentFilter(SENT_SMS_ACTION));
		
		// 监听接收状态
		String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		PendingIntent deliverPI = PendingIntent.getBroadcast(ctx, 0, new Intent(DELIVERED_SMS_ACTION), 0);
		ctx.registerReceiver(new BroadcastReceiver()
		{
			public void onReceive(Context _ctx, Intent _it)
			{
				Logger.i("收信人已经成功接收");
			}
		}, new IntentFilter(DELIVERED_SMS_ACTION));
		
		// 获取短信管理器
		android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
		// 拆分短信内容（手机短信长度限制）
		List<String> divideContents = smsManager.divideMessage(body);
		for (String text : divideContents)
		{
			smsManager.sendTextMessage(phone, null, text, sentPI, deliverPI);
		}
	}

	public static void startBrowser(Context ctx, final String url)
	{
		try
		{
			if(url == null) return;
			String urlt = "";
			if(!url.startsWith("http://"))
			{
				urlt = "http://" + url;
			}
			else
			{
				urlt = url;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.setData(Uri.parse(urlt));
			ctx.startActivity(intent);
		}
		catch(Exception e1)
		{
			Logger.e("startBrowser() " + e1.toString());
		}
	}
	
	//发送彩信,设备会提示选择合适的程序发送 
//	Uri uri = Uri.parse("content://media/external/images/media/23"); 
//	//设备中的资源（图像或其他资源） 
//	Intent intent = new Intent(Intent.ACTION_SEND); 
//	intent.putExtra("sms_body", "内容"); 
//	intent.putExtra(Intent.EXTRA_STREAM, uri); 
//	intent.setType("image/png"); 
//	startActivity(it);
	
	 //Email 
//	Intent intent=new Intent(Intent.ACTION_SEND); 
//	String[] tos={"android1@163.com"}; 
//	String[] ccs={"you@yahoo.com"}; 
//	intent.putExtra(Intent.EXTRA_EMAIL, tos); 
//	intent.putExtra(Intent.EXTRA_CC, ccs);
//	 intent.putExtra(Intent.EXTRA_TEXT, "The email body text"); 
//	intent.putExtra(Intent.EXTRA_SUBJECT, "The email subject text"); 
//	intent.setType("message/rfc822"); 
//	startActivity(Intent.createChooser(intent, "Choose Email Client"));
	
	//选择图片 requestCode 返回的标识
//	Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
//	intent.setType(contentType); //查看类型 String IMAGE_UNSPECIFIED = "image/*";
//	Intent wrapperIntent = Intent.createChooser(intent, null);
//	((Activity) context).startActivityForResult(wrapperIntent, requestCode);
	
	//添加音频
//	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//	intent.setType(contentType); //String VIDEO_UNSPECIFIED = "video/*";
//	Intent wrapperIntent = Intent.createChooser(intent, null);
//	((Activity) context).startActivityForResult(wrapperIntent, requestCode);  
	
	 //拍摄视频 
//	int durationLimit = getVideoCaptureDurationLimit(); //SystemProperties.getInt("ro.media.enc.lprof.duration", 60);
//	Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//	intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
//	intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);
//	startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
	
	//视频
//	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//	intent.setType(contentType); //String VIDEO_UNSPECIFIED = "video/*";
//	Intent wrapperIntent = Intent.createChooser(intent, null);
//	((Activity) context).startActivityForResult(wrapperIntent, requestCode);  
	
	//录音
//	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//	intent.setType(ContentType.AUDIO_AMR); //String AUDIO_AMR = "audio/amr";
//	intent.setClassName("com.android.soundrecorder",
//	"com.android.soundrecorder.SoundRecorder");
//	((Activity) context).startActivityForResult(intent, requestCode); 
	
	//拍照 REQUEST_CODE_TAKE_PICTURE 为返回的标识
//	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //"android.media.action.IMAGE_CAPTURE";
//	intent.putExtra(MediaStore.EXTRA_OUTPUT, Mms.ScrapSpace.CONTENT_URI); // output,Uri.parse("content://mms/scrapSpace");
//	startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);  
	
//	动态加载权限
//	PackageManager.addPermission()
	
	// 启动选择壁纸来源
	public static void startWallpaperChooser(Context ctx)
	{
		try
		{
		    Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		    pickWallpaper.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		    Intent chooser = Intent.createChooser(pickWallpaper, "选择壁纸来源");
		    ctx.startActivity(chooser);
		}
		catch(Exception e1)
		{
			Logger.e("启动选择壁纸器出错" + e1.toString());
		}
	}
	
	// 启动显示全部程序的管理设置
	@SuppressLint("InlinedApi")
	public static void startAllappsSetting(Context ctx)
	{
		try
		{
		    Intent manageApps = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
		    manageApps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    ctx.startActivity(manageApps);
		}
		catch(Exception e1)
		{
			Logger.e("启动显示全部程序的管理设置" + e1.toString());
		}
	}
	
	// 启动系统设置
	public static void startSystemSetting(Context ctx)
	{
		try
		{
	        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
	        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	        ctx.startActivity(settings);
		}
		catch(Exception e1)
		{
			Logger.e("启动系统设置" + e1.toString());
		}
	}
	
	//启动下载管理界面TODO:
	public static void startDownloadUI(Context ctx)
	{
		try
		{
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("启动下载管理UI " + e1.toString());
		}
	}
	
	@SuppressLint ("SdCardPath")
	// 需要将 /res/raw/zlsu 拷贝到你工程的相应目录下
	public static void rootSU(Context ctx, final int zlsuId)
	{
		try
		{
			final String ROOT_SU = "zlsu";
			File zlsu = new File("/system/bin/" + ROOT_SU);
			InputStream suStream = ctx.getResources().openRawResource(zlsuId);
			/**
			 * 如果zlsu存在，则和raw目录下的zlsu比较大小，大小相同则不替换
			 */
			if (zlsu.exists())
			{
				if (zlsu.length() == suStream.available())
				{
					suStream.close();
					return;
				}
			}

			/**
			 * 先把zlsu 写到/data/data/com.zl.movepkgdemo中 然后再调用 su 权限 写到
			 * /system/bin目录下
			 */
			byte[] bytes = new byte[suStream.available()];
			DataInputStream dis = new DataInputStream(suStream);
			dis.readFully(bytes);
			suStream.close();
			dis.close();
			
			String pkgPath = ctx.getApplicationContext().getPackageName();
			// "/data/data/com.zl.movepkgdemo/zlsu"
			String zlsuPath = "/data/data/" + pkgPath + File.separator + ROOT_SU;
			File zlsuFileInData = new File(zlsuPath);
			if (!zlsuFileInData.exists())
			{
				Logger.i(zlsuPath + " not exist! ");
				try
				{
					Logger.i("creating " + zlsuPath + "......");
					zlsuFileInData.createNewFile();
					Logger.i("create " + zlsuPath + " successfully ! ");
				}
				catch (IOException e1)
				{
					Logger.e("create " + zlsuPath + " failed ! " + e1.toString());
				}
			}
			FileOutputStream suOutStream = new FileOutputStream(zlsuPath);
			suOutStream.write(bytes);
			suOutStream.close();

			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
			// "busybox cp /data/data/com.zl.movepkgdemo/zlsu /system/bin/zlsu \n"
			os.writeBytes("busybox cp " + zlsuPath + " /system/bin/" + ROOT_SU + "\n");
			// "busybox chown 0:0 /system/bin/zlsu \n"
			os.writeBytes("busybox chown 0:0 /system/bin/" + ROOT_SU + "\n");
			// "chmod 4755 /system/bin/zlsu \n"
			os.writeBytes("chmod 4755 /system/bin/" + ROOT_SU + "\n");
			os.writeBytes("exit\n");
			os.flush();
			os.close();
		}
		catch (Exception e1)
		{
			// Toast toast = Toast
			// .makeText(ctx, e.getMessage(), Toast.LENGTH_LONG);
			// toast.show();
			Logger.e("RootUtil preparezlsu: error " + e1.toString());
		}
	}
	
	// 判断是否具有ROOT权限
	public static boolean isRoot()
	{
		boolean res = false;
		try
		{
			if ( (!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists()))
			{
				res = false;
			}
			else
			{
				res = true;
			}
		}
		catch (Exception e1)
		{
			Logger.e("isRoot:" + e1.toString());
		}
		return res;
	}
	
	/** 
	 * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限) 
	 *  
	 * @return 应用程序是/否获取Root权限 
	 * 
	 * 调用 upgradeRootPermission(getPackageCodePath());
	 */  
//	public static boolean upgradeRootPermission(String pkgCodePath)
//	{
//		Process process = null;
//		DataOutputStream os = null;
//		try
//		{
//			String cmd = "chmod 777 " + pkgCodePath;
//			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
//			os = new DataOutputStream(process.getOutputStream());
//			os.writeBytes(cmd + "\n");
//			os.writeBytes("exit\n");
//			os.flush();
//			process.waitFor();
//		}
//		catch (Exception e)
//		{
//			return false;
//		}
//		finally
//		{
//			try
//			{
//				if (os != null)
//				{
//					os.close();
//				}
//				process.destroy();
//			}
//			catch (Exception e)
//			{
//			}
//		}
//		return true;
//	}
	// 通过包名/cls查询已安装程序的activity info
//	public CellInfo queryApp(final String pkgName, final String cls)
//	{
//		if(pkgName == null || cls == null) return null;
//		CellInfo cell = null;
//		try
//		{
//			cell = htVal.get(cls);
//			if(cell != null) return cell;
//			
//			PackageManager pm = ctx.getPackageManager();
//			ActivityInfo ai = pm.getActivityInfo(new ComponentName(pkgName, cls) , PackageManager.GET_ACTIVITIES);
//			
//			cell = new CellInfo();
//			cell.cls = ai.name; // 获得该应用程序的启动Activity的name
//			cell.pkgName = ai.packageName; // 获得应用程序的包名
//			cell.label = (String)ai.loadLabel(pm); // 获得应用程序的Label
//			
//			if(paint.measureText(cell.label) > cellWidth)
//			{
//				cell.displayName = StrUtil.breakText(cell.label, cellWidth, paint);
//			}
//			else
//			{
//				cell.displayName = cell.label;
//			}
//			
//			Drawable icon = ai.loadIcon(pm); // 获得应用程序图标
//			cell.icon = ((BitmapDrawable)icon).getBitmap();
//		}
//		catch(Exception e1)
//		{
//			Logger.e("queryApp() " + e1.toString());
//		}
//		return cell;
//	}
	
	/** 检测相机是否存在 */
	public static boolean hasCamera(Context ctx)
	{
		try
		{
			if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
			{
				// 摄像头存在
				return true;
			}
		}
		catch (Exception e1)
		{
			Logger.e("checkCamera() " + e1.toString());
		}
		return false;
	}
	
	// 打开相机
	public static boolean startCamera(Context ctx)
	{
		try
		{
			PackageManager pm = ctx.getPackageManager();
			Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			it.addCategory(Intent.CATEGORY_DEFAULT);
			List<ResolveInfo> resolveInfos = pm.queryIntentActivities(it, 0);
			for (ResolveInfo resolveInfo : resolveInfos)
			{
//				if((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
				{
					it.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
					
					it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					it.setAction(Intent.ACTION_MAIN);
					ctx.startActivity(it);
					break;
				}
			}
			return true;
		}
		catch(Exception e1)
		{
			Logger.e("startCamera()" + e1.toString());
		}
		return false;
	}
	// 如果安装了两个以上相机,会弹出选择界面
	public static boolean startCameraChoose(Context ctx)
	{
		try
		{
			Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ctx.startActivity(it);
			return true;
		}
		catch(Exception ext)
		{
			Logger.e("startCameraChooser() " + ext.toString());
		}
		return false;
	}
	// 启动系统默认桌面TODO:
	public static void startDefaultLauncher(final Context ctx)
	{
		try
		{
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_HOME);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startTestingSettings() " + e1.toString());
		}
	}
	
	// *#*#4636#*#* 命令行调起的测试界面
	public static void startTestingSettings(final Context ctx)
	{
		try
		{
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setComponent(new ComponentName("com.android.settings", "com.android.settings.TestingSettings"));
			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startTestingSettings() " + e1.toString());
		}
	}
	
	// ####1111# 命令行调起的工厂测试工具
	public static void startMTKTestTool(final Context ctx)
	{
		try
		{
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setComponent(new ComponentName("com.android.huaqin.factory", "com.android.huaqin.factory.ControlCenterActivity"));
			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startMTKTestTool() " + e1.toString());
		}
	}
	
	// *#86556# 命令行调起的版本信息查看
	public static void startMTKVersion(final Context ctx)
	{
		try
		{
//			Intent it = new Intent(Intent.ACTION_MAIN);
//			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			it.setComponent(new ComponentName("com.android.dialer", "com.android.dialer.DialtactsActivity"));
//			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startMTKVersion() " + e1.toString());
		}
	}
	
	// *#*#3646633#*#* 命令行调起 EngineerMode
	public static void startMTKEngineerMode(final Context ctx)
	{
		try
		{
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.setComponent(new ComponentName("com.mediatek.engineermode", "com.mediatek.engineermode.EngineerMode"));
			ctx.startActivity(it);
		}
		catch(Exception e1)
		{
			Logger.e("startMTKTestTool() " + e1.toString());
		}
	}
	
	@SuppressLint ("InlinedApi")
	public static void startAppOps(final Context ctx)
	{
		try
		{
			switch(android.os.Build.VERSION.SDK_INT)
			{
				case android.os.Build.VERSION_CODES.KITKAT: // 4.4
					// 测试,失败
					Intent intent = new Intent();
					intent.setClassName("com.android.settings", "com.android.settings.Settings");
					intent.setAction(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					intent.putExtra(":android:show_fragment", "com.android.settings.applications.AppOpsSummary");
					ctx.startActivity(intent);
					
					break;
				case android.os.Build.VERSION_CODES.JELLY_BEAN_MR2: // 4.3
					// 测试成功
				    Intent localIntent = new Intent();
				    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				    localIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AppOpsSummaryActivity"));
				    localIntent.setAction("android.intent.action.VIEW");
				    ctx.startActivity(localIntent);
					
				    break;
			}
		}
		catch(Exception e1)
		{
			Logger.e("startAppOps() " + e1.toString());
		}
	}
	
	// 通话设置部分
//	intent.setClassName("com.android.phone", "com.mediatek.settings.CallSettings"); // 总的通话设置
//	intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting"); // 通话设置-语音电话
//	intent.setClassName("com.android.phone", "com.mediatek.settings.OthersSettings"); // 通话设置-其他设置
//	intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsCallForwardOptions"); // 语音电话设置-来电转接
//	intent.setClassName("com.android.phone", "com.mediatek.settings.CallBarring"); // 语音电话设置-呼叫限制
//	intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsAdditionalCallOptions"); // 语音电话设置-其他设置
    
	// 通话设置-语音电话&通话设置(MTK&QCOM)
    public static void startCallFeaturesSetting(Context ctx)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting"); // 通话设置-语音电话
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ctx.startActivity(intent);
    	}
        catch(Exception ext)
		{
			Logger.e("startMediatekCallFeaturesSetting() " + ext.toString());
		}
    }
    // 通话设置-其他设置(MTK)
    public static void startMediatekOthersSettings(Context ctx)
    {
    	try
    	{
    		// 通话设置-其他设置
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) // 21 == 5.0
        	{
        		intent.setClassName("com.android.phone", "com.mediatek.settings.OthersSettings");
        	}
        	else
        	{
        		intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsAdditionalCallOptions");
        	}
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ctx.startActivity(intent);
    	}
    	catch(Exception ext)
    	{
    		Logger.e("startMediatekOthersSettings() " + ext.toString());
    	}
    }
    // 来电转接(高通)
    public static void startQcomGsmUmtsCallForwardOptions(Context ctx)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsCallForwardOptions"); // 语音电话设置-来电转接
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ctx.startActivity(intent);
    	}
    	catch(Exception ext)
    	{
    		Logger.e("startQcomGsmUmtsCallForwardOptions() " + ext.toString());
    	}
    }
    // 语音通话-其他设置(高通)
    public static void startQcomOthersSettings(Context ctx)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsAdditionalCallOptions"); // 语音电话设置-其他设置
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ctx.startActivity(intent);
    	}
    	catch(Exception ext)
    	{
    		Logger.e("startQcomGsmUmtsCallForwardOptions() " + ext.toString());
    	}
    }
    
    // Activity如何返回数据
    // 一般其他程序要数据会发 Intent.ACTION_GET_CONTENT 这个action要过滤到这个action从而进入编辑模式,选择后再finish()调用之前写如下代码
//	Intent intent = new Intent();
//	intent.setData(Uri.fromFile(new File("/dodo/")));
//	setResult(RESULT_OK, intent);
//	finish();
    
    // 通知栏右侧显示/隐藏小闹钟图标,仅支持 < 5.0
    public static void setStatusBarAlarm(final Context ctx, final boolean enabled)
	{
    	try
    	{
    		Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED"); // Intent.ACTION_ALARM_CHANGED
    		alarmChanged.putExtra("alarmSet", enabled);
    		ctx.sendBroadcast(alarmChanged);
    	}
    	catch(Exception ext)
    	{
    		Logger.e("setStatusBarAlarm() " + ext.toString());
    	}
    }
    // 接听电话 http://stackoverflow.com/questions/2610587/how-to-programmatically-answer-a-call
    public static void answerCall(final Context ctx)
    {
    	try
    	{
    		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
    		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
    		ctx.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    		
    		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
    		headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
    		headSetUnPluggedintent.putExtra("state", 0);
    		headSetUnPluggedintent.putExtra("name", "Headset");
    		ctx.sendOrderedBroadcast(headSetUnPluggedintent, null);
    	}
    	catch (Exception ext)
    	{
    		Logger.e("answerCall() " + ext.toString());
    	}
    }
    // 接听电话,需要root
    public static boolean answerIncomeCall()
    {
		return handleIncomeCall(5);
    }
    // 挂断电话,需要root
    public static boolean endIncomeCall()
    {
		return handleIncomeCall(6);
    }
    private static boolean handleIncomeCall(final int keyevent)
    {
		try
		{
//			Thread.sleep(800);
//			new String[] { "su", "-c", "input keyevent 5"} 接听
//			new String[] { "su", "-c", "input keyevent 6"} 挂断
			Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "input keyevent " + keyevent });
			return process.waitFor() == 0 ? true : false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
    }
    
    // 分享
    // 分享字符串
    public static void shareText(Context ctx, final String sContent)
    {
    	if(ctx == null || sContent == null) return;
    	
    	try
    	{
    		Intent sendIntent = new Intent();
    		sendIntent.setAction(Intent.ACTION_SEND);
    		sendIntent.putExtra(Intent.EXTRA_TEXT, sContent);
    		sendIntent.setType("text/plain");
    		ctx.startActivity(Intent.createChooser(sendIntent, "分享到"));
    	}
    	catch(Exception ext)
    	{
    		Logger.e("shareText()" + ext.toString());
    	}
    }
    // 分享图片
    public static void shareImg(Context ctx, final String sPath)
    {
    	if(ctx == null || Empty.isEmpty(sPath)) return;
    	
    	try
    	{
    		File file = FileUtil.isExists(sPath);
    		if(file != null)
    		{
            	Intent shareIntent = new Intent(Intent.ACTION_SEND);  
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, List);
                ctx.startActivity(Intent.createChooser(shareIntent, "分享到"));
    		}
    	}
    	catch(Exception ext)
    	{
    		Logger.e("shareImg()" + ext.toString());
    	}
    }
    @SuppressLint("InlinedApi")
	public static void shareHtml(Context ctx, final String sContent)
    {
    	if(ctx == null || sContent == null) return;
    	
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
    	{
        	try
        	{
        		Intent sendIntent = new Intent();
        		sendIntent.setAction(Intent.ACTION_SEND);
        		sendIntent.putExtra(Intent.EXTRA_HTML_TEXT, sContent);
        		sendIntent.setType("text/html");
        		ctx.startActivity(Intent.createChooser(sendIntent, "分享到"));
        	}
        	catch(Exception ext)
        	{
        		Logger.e("shareText()" + ext.toString());
        	}
    	}
    }
    public static void shareStream(Context ctx, final String abspath)
    {
		try
		{
			File file = FileUtil.isExists(abspath);
			if(file == null) return;

			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			shareIntent.setType("*/*");
			ctx.startActivity(shareIntent);
		}
		catch(Exception ext)
		{
			Logger.e("share()" + ext.toString());
		}
    }
    // 检查时候是模拟器
    public static boolean isEmulator()
	{
		String result = "";
		try
		{
			String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
			ProcessBuilder cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			StringBuffer sb = new StringBuffer();
			String readLine = "";
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
			while ( (readLine = responseReader.readLine()) != null)
			{
				sb.append(readLine);
			}
			responseReader.close();
			result = sb.toString().toLowerCase(Locale.ENGLISH);
		}
		catch (Exception ext)
		{
			Logger.e("isEmulator()" + ext.toString());
		}
		return (!result.contains("arm")) || (result.contains("intel")) || (result.contains("amd"));
	}
    // 检查蓝牙是否连接设备
    public static boolean isBluetoothConnected()
	{
    	try
    	{
    		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        	
    		// 蓝牙适配器是否存在，即是否发生了错误
    		if (ba == null)
    		{
    			// "蓝牙出现问题";
    		}
    		else
    		{
    			if (ba.isEnabled())
    			{
    				int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP); // 可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
    				int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET); // 蓝牙头戴式耳机，支持语音输入输出
    				int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH); // 蓝牙穿戴式设备

    				// 查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
    				int flag = -1;
    				if (a2dp == BluetoothProfile.STATE_CONNECTED)
    				{
    					flag = a2dp;
    				}
    				else if (headset == BluetoothProfile.STATE_CONNECTED)
    				{
    					flag = headset;
    				}
    				else if (health == BluetoothProfile.STATE_CONNECTED)
    				{
    					flag = health;
    				}
    				// 说明连接上了三种设备的一种
    				if (flag != -1)
    				{
    					return true;
    				}
    				else if (flag == -1)
    				{
//    					ConnectivityManager cm = (ConnectivityManager) at.getSystemService(Context.CONNECTIVITY_SERVICE);
//    					NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

//    					if (netInfo == null)
//    					{
    						// "蓝牙不可用";
//    					}
//    					else
//    					{
//    						State st = netInfo.getState();
    						// "未知情况";
//    					}
    				}
    			}
    			else
    			{
    				// "蓝牙未开启";
    			}
    		}
    	}
    	catch(Exception ext)
    	{
    		Logger.e("" + ext.toString());
    	}
    	return false;
	}
    /** 获取已安装程序的签名的MD5摘要 */
	public static String getSignatureDigest(final Context ctx, final String pkgName)
	{
		if(ctx == null || Empty.isEmpty(pkgName)) return null;
		try
		{
			PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
			Signature sign = pkgInfo.signatures[0];
			if(sign != null)
			{
				MessageDigest md5 = MessageDigest.getInstance("MD5");
	            byte[] digest = md5.digest(sign.toByteArray()); // get digest with md5 algorithm
	            return StrUtil.byte2hex(digest);
			}
		}
		catch (Exception ext)
		{
			Logger.e("signatureDigest() " + ext.toString());
		}
		return null;
	}
}
