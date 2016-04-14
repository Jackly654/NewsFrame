package hz.dodo;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class HZDodo
{
	Activity at;
	int fw, fh, sbh;
	public static int sill = 15; // 移动的最小阀值
	public float density;
	
	public HZDodo(Activity at, boolean fullScreen)
	{
		this.at = at;
		DisplayMetrics metrics = new DisplayMetrics();
		at.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;
//		sill = 15;
//		sill *= density;

//		Rect frame = new Rect();
//		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

		fh = metrics.heightPixels;
		fw = metrics.widthPixels;
		if (fh < fw)
		{
			fw ^= fh;
			fh ^= fw;
			fw ^= fh;
		}
		sill = fw/32;
		
		if(!fullScreen)
		{
			sbh = getSBar();
			fh -= sbh;
		}
	}
	
	public int getFw(){return fw;}
	
	public int getFh(){return fh;}
	
	public float getDensity(){return density;}
	
	public int getSBar()
	{
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try
		{
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = at.getResources().getDimensionPixelSize(x);
		}
		catch (Exception e1)
		{
			Logger.e("getSBar()=" + e1.toString());
		}
		return sbar;
	}
	
	public static int getSBar(Context ctx)
	{
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try
		{
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = ctx.getResources().getDimensionPixelSize(x);
		}
		catch (Exception e1)
		{
			Logger.e("getSBar()=" + e1.toString());
		}
		return sbar;
	}
	
	// 获取ActionBar的高度 4.4底部虚拟按键的高度(BACK,HOME,MENU)
	@SuppressWarnings ("unused")
	private static int getActionBarHeight(Context ctx)
	{
		try
		{
//			if(ViewConfiguration.get(ctx).hasPermanentMenuKey()) // 是否有menu
			if(ctx.getResources().getBoolean(17891374)) // ctx.getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
			{
				// 是否有navigation bar资源
				TypedValue tv = new TypedValue();
				int actionBarHeight = 0;
				if (ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
				{
					actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, ctx.getResources().getDisplayMetrics());
				}
				return actionBarHeight;
			}
		}
		catch(Exception e1)
		{
			Logger.i("getActionBarHeight() " + e1.toString());
		}
		return 0;
	}
	
	// 获取底部虚拟按键区域的高度
	public static int getNavigationBarHeight(Context ctx, WindowManager wm)
	{
		try
		{
			DisplayMetrics metrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(metrics);

			int fh = metrics.heightPixels;
			int fw = metrics.widthPixels;
			if (fh < fw)
			{
				fw ^= fh;
				fh ^= fw;
				fw ^= fh;
			}

			int fh2 = getScreentHeight(wm); // 屏幕的物理高度PIX
			
			// navigation height = fh2 - fh
			return fh2 - fh;
		}
		catch(Exception e1)
		{
			Logger.e("getNavigationBarHeight() " + e1.toString());
		}
		return 0;
	}
	
	// 该方法获取屏幕的物理高度(包括status bar & navigation bar)
	public static int getScreentHeight(WindowManager wm)
	{
		if(wm == null) return 0;
		int heightPixels = 0;
		Display d = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;
		heightPixels = metrics.heightPixels;
		// includes window decorations (statusbar bar/navigation bar)
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
		{
			try
			{
				heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
			}
			catch (Exception e1)
			{
				Logger.e("invoke getRawHeight " + e1.toString());
			}
		}
		// includes window decorations (statusbar bar/navigation bar)
		else if (Build.VERSION.SDK_INT >= 17)
		{
			try
			{
				android.graphics.Point realSize = new android.graphics.Point();
				Display.class.getMethod("getRealSize", android.graphics.Point.class).invoke(d, realSize);
				heightPixels = realSize.y;
			}
			catch (Exception e1)
			{
				Logger.e("invoke getRealSize " + e1.toString());
			}
		}
		return heightPixels;
	}
	
	// 全屏/非全屏的切换
	public static void chgScreen(Activity at, boolean full)
	{
		if (full)
		{
			WindowManager.LayoutParams params = at.getWindow().getAttributes();
			params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			at.getWindow().setAttributes(params);
			at.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
		else
		{
			WindowManager.LayoutParams params = at.getWindow().getAttributes();
			params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			at.getWindow().setAttributes(params);
			at.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}
	
	// 虚拟按键的菜单按钮
	public static void addVMenu(Activity at)
	{
		try
		{
			at.getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		}
		catch (NoSuchFieldException e1)
		{
			Logger.e("addVMenu() NoSuchFieldException " + e1.toString());
		}
		catch (IllegalAccessException e1)
		{
			Logger.e("addVMenu() IllegalAccessException " + e1.toString());
		}
	}
}
