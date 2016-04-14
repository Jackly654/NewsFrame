package hz.dodo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

// ApplicationInfo ai = ctx.getApplicationInfo();
public class DContext
{
	final String DIR_DRAWABLE = "drawable";
	final String DIR_ANIM = "anim";
	
	Context friendCtx;
	String friendPkgName;
	
	HashMap<String, Bitmap> hmBm;
	HashMap<String, Integer> hmClr;
//	HashMap<String, Integer> htAnim; // res/anim/**.xml ID
	
	public DContext(final Context ctx, final String pkgName)
	{
		try
		{
			if(pkgName == null) return;
			
			hmBm = new HashMap<String, Bitmap>();
			hmClr = new HashMap<String, Integer>();
//			htAnim = new HashMap<String, Integer>();
			
			friendPkgName = pkgName;
			if(PkgMng.isInstalled(ctx, pkgName))
			{
				friendCtx = ctx.createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY);
			}
			
			long l1 = System.currentTimeMillis();
			initColor();
			Logger.i("颜色耗时:" + (System.currentTimeMillis() - l1));
//			initAnim();
		}
		catch(Exception ext)
		{
			friendCtx = null;
			Logger.e("DContext() " + pkgName + " " + ext.toString());
		}
	}
	
	public void destroy()
	{
		if(hmBm != null)
		{
			hmBm.clear();
		}
		if(hmClr != null)
		{
			hmClr.clear();
		}
	}
	
	public Bitmap getDrawableImage(final String imgName)
	{
		try
		{
			if(friendCtx == null || imgName == null || friendPkgName == null) return null;
			
			Bitmap bm = hmBm.get(DIR_DRAWABLE + imgName);
			if(bm != null) return bm;
			
			int resID = friendCtx.getResources().getIdentifier(imgName, DIR_DRAWABLE, friendPkgName);
			bm = BitmapFactory.decodeResource(friendCtx.getResources(), resID);
			if(bm != null)
			{
				hmBm.put(DIR_DRAWABLE + imgName, bm);
				return bm;
			}
		}
		catch(Exception ext)
		{
			Logger.e("getDrawableRes()" + ext.toString());
		}
		return null;
	}

	private void initColor()
	{
		if(friendCtx == null) return;

		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		try
		{
			is = friendCtx.getResources().getAssets().open("colors.txt");
			isr = new InputStreamReader(is);
			bufR = new BufferedReader(isr);
			
			String[] sArr;
			String[] argb;
			int a, r, g, b, clr;
			
			String line = "";
			while ( (line = bufR.readLine()) != null)
			{
				try
				{
					line = line.trim();
					if(!line.startsWith("*"))
					{
						sArr = line.split("=");
						if(sArr != null && sArr.length == 2)
						{
							argb = sArr[1].split(",");
							if(argb != null && argb.length == 4)
							{
								a = Integer.parseInt(argb[0], 16);
								r = Integer.parseInt(argb[1], 16);
								g = Integer.parseInt(argb[2], 16);
								b = Integer.parseInt(argb[3], 16);
								
								clr = Color.argb(a, r, g, b);
								
								hmClr.put(sArr[0], Integer.valueOf(clr));
							}
						}
					}
				}
				catch(Exception ext)
				{
					Logger.e("readLine() " + ext.toString());
				}
			}
		}
		catch(Exception ext)
		{
			Logger.e("initColor() " + ext.toString());
		}
		finally
		{
			if(isr != null)
			{
				try { if(isr != null) isr.close(); }
				catch (IOException ext) { ext.printStackTrace(); }
				
				try { if(bufR != null) bufR.close(); }
				catch (IOException ext) { ext.printStackTrace(); }
				
				try { if(is != null) is.close(); }
				catch (IOException ext) { ext.printStackTrace(); }
			}
		}
	}
	
//	private void initAnim()
//	{
//		int resID = friendCtx.getResources().getIdentifier("zoomin", DIR_ANIM, friendPkgName);
//		Logger.i("resID:" + resID);
//	}
	
	public int getClr(final String key)
	{
		if(key == null) return 0;
		Integer itg = hmClr.get(key);
		if(itg != null) return itg.intValue();
		return 0;
	}
	
//	3. 图片放在src目录下
//　　String path = "com/xiangmu/test.png"; //图片存放的路径
//　　InputStream is = getClassLoader().getResourceAsStream(path); //得到图片流
	
//	4.android中有个Assets目录,这里可以存放只读文件
//	资源获取的方式为
//	InputStream is = getResources().getAssets().open(name)；
}
