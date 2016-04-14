package hz.dodo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;

public class SystemUtil
{
	private static String macaddress, phonenum, imei, v_code, v_name, pkgname;
	
	public interface ScreenshotCallback
	{
		public void sendMsg(final int code, final String msg);
	}
	
	public static String getSendLogBaseInfo(Context ctx)
	{
		return "mac@@" + getMacAddress(ctx)		// mac address
				+ ",,imei@@" + getIMEI(ctx)		// imei
				+ ",,phone@@" + getPhoneNum(ctx) // phone
//				+ ",,vcode@@" + getVCode(ctx, null) 	// version code
//				+ ",,vname@@" + getVName(ctx, null)	// version name
				+ ",,model@@" + Build.MODEL	// 手机厂商型号
				+ ",,release@@" + android.os.Build.VERSION.RELEASE; // 系统版本号
//				+ "&timer=" + System.currentTimeMillis();  // 时间点
	}
	
	public static String getMacAddress(Context ctx)
	{
		if(macaddress != null) return macaddress;
		try
		{
			WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null)
			{
				macaddress = wifiInfo.getMacAddress();
				return macaddress;
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		return "0";
	}
	
	public static String getIMEI(Context ctx)
	{
		if(imei != null) return imei;
		try
		{
			TelephonyManager telmng = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			if(telmng != null)
			{
				imei = telmng.getDeviceId();
				return imei;
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		return "0";
	}
	
	public static String getPhoneNum(Context ctx)
	{
		if(phonenum != null) return phonenum;
		try
		{
			TelephonyManager telmng = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			if(telmng != null)
			{
				String num = telmng.getLine1Number();
				if(num != null && !"null".equals(num))
				{
					if(num.startsWith("+86"))
					{
						num = num.substring("+86".length());
					}
					phonenum = num;
					return phonenum;
				}
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		return "0";
	}
	
	public static String getVCode(final Context ctx, String pkgname)
	{
		if(v_code != null) return v_code;
		try
		{
			if(pkgname == null) pkgname = getPkgName(ctx);
			PackageInfo info = ctx.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES);
			if (info != null)
			{
				v_code = "" + info.versionCode;
				return v_code;
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		return "";
	}
	
	public static String getVName(final Context ctx, String pkgname)
	{
		if(v_name != null) return v_name;
		try
		{
			if(pkgname == null) pkgname = getPkgName(ctx);
			PackageInfo info = ctx.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES);
			if (info != null)
			{
				v_name = info.versionName;
				return v_name;
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		return "";
	}
	
	public static String getPkgName(final Context ctx)
	{
		if(pkgname != null) return pkgname;
		pkgname = ctx.getPackageName();
		return pkgname;
	}
	
	public static void addShortCut(final Activity at, final String appName, final int iconId)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
					// 不允许重复创建  
			        shortcut.putExtra("duplicate", false);

			        // 快捷方式的名称 
			        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName/*ctx.getString(R.string.app_name)*/);
			        // 快捷方式的图标  
			        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(at, iconId/*R.drawable.ic_launcher*/);
			        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

			        // 换种方式创建快捷方式，修复go桌面无法打开应用的问题
			        Intent target = new Intent(at.getApplicationContext(), at.getClass());
			        target.setAction(Intent.ACTION_MAIN);

			        // 防止启动两次
			        target.addCategory(Intent.CATEGORY_LAUNCHER);
			        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, target);
			        at.sendBroadcast(shortcut);
				}
				catch(Exception e1)
				{
					Logger.e("addShortCut()=" + e1.toString());
				}
			}
		}.start();
	}
	
	public static void screenshot(final ScreenshotCallback callback, final Context ctx)
	{
		new Thread()
		{
			public void run()
			{
				FileInputStream buf = null;
				DataOutputStream dos = null;
				DataInputStream dStream = null;
				FileOutputStream fos = null;
				try
				{
					Logger.i("准备截屏");
					
					// 设置文件权限
					// 执行su变更用户身份为root
					Process process = Runtime.getRuntime().exec("su");
					// 转成DataOutputStream方便写入字符串
					dos = new DataOutputStream(process.getOutputStream());
					
					dos.writeBytes("chmod 777 /dev/graphics/fb0\n");
					dos.flush();
					
					buf = new FileInputStream(new File("/dev/graphics/fb0"));// 读取文件内容

					DisplayMetrics dm = new DisplayMetrics();
					Display display = ((Activity)ctx).getWindowManager().getDefaultDisplay();
					display.getMetrics(dm);
					int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
					int screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
					@SuppressWarnings ("deprecation")
					int pixelformat = display.getPixelFormat();
					PixelFormat localPixelFormat1 = new PixelFormat();
					PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
					int deepth = localPixelFormat1.bytesPerPixel;// 位深
					byte[] piex = new byte[screenHeight * screenWidth * deepth];// 像素
					dStream = new DataInputStream(buf);
					dStream.readFully(piex);
					
					int[] colors = new int[screenHeight * screenWidth];
					// 将rgb转为色值
					
					int i1 = 0;
					while(i1 < colors.length)
					{
						int b = (piex[i1 * 4] & 0xFF);
						int g = (piex[i1 * 4 + 1] & 0xFF);
						int r = (piex[i1 * 4 + 2] & 0xFF);
						int a = (piex[i1 * 4 + 3] & 0xFF);
						colors[i1] = (a << 24) + (r << 16) + (g << 8) + b;
						++i1;
					}

					// 得到屏幕bitmap
					Bitmap bm = Bitmap.createBitmap(colors, screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
					if(bm != null)
					{
						String filename = SDCard.getSDCardRootPath(ctx) + "/dodo/SmartKey/[闪键]" + System.currentTimeMillis() + ".png";
						File dirfile = new File(filename.substring(0, filename.lastIndexOf("/")));
						// 如果不存在
						if (!dirfile.exists()) dirfile.mkdirs(); // 创建多级目录结构
						
						fos = new java.io.FileOutputStream(filename);
						bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
						Logger.i("写文件结束");
						if(callback != null) callback.sendMsg(0, filename);
					}
					else
					{
						Logger.e("创建BITMAP失败");
						if(callback != null) callback.sendMsg(-3, "创建Bitmap失败");
					}
				}
				catch (FileNotFoundException e1)
				{
					Logger.e("screenshot() FileNotFoundException " + e1.toString());
					if(callback != null) callback.sendMsg(-1, "screenshot() FileNotFoundException " + e1.toString());
				}
				catch (IOException e1)
				{
					Logger.e("screenshot() IOException " + e1.toString());
					if(callback != null) callback.sendMsg(-2, "screenshot() IOException " + e1.toString());
				}
				finally
				{
					if (buf != null)
					{
						try
						{
							buf.close();
						}
						catch (IOException e1)
						{
							Logger.e("screenshot() finally buf.close() " + e1.toString());
						}
					}
					if (dos != null)
					{
						try
						{
							dos.close();
						}
						catch (IOException e1)
						{
							Logger.e("screenshot() finally dos.close() " + e1.toString());
						}
					}
					if (dStream != null)
					{
						try
						{
							dStream.close();
						}
						catch (IOException e1)
						{
							Logger.e("screenshot() finally dStream.close() " + e1.toString());
						}
					}
					if (fos != null)
					{
						try
						{
							fos.close();
						}
						catch (IOException e1)
						{
							Logger.e("screenshot() finally fos.close() " + e1.toString());
						}
					}
				}
			};
		}.start();
	}
}
