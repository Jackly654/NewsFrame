package hz.dodo;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import dalvik.system.DexClassLoader;

public class ApkParser
{
	public interface Callback
	{
		public void initState(final boolean initState);
	}

	private final String ASSETSMANAGERPATH = "android.content.res.AssetManager";
	private final String ADDASSETPATH = "addAssetPath";

	private AssetManager am;
	private Context context;
	private String apkDir;
	private HashMap<String, Bitmap> hmBms;
	private boolean initState = false;

	public ApkParser(final Context context, final String apkDir, final Callback callback)
	{
		this.context = context;
		if (null == context || null == apkDir || null == FileUtil.isExists(apkDir))
			initState = false;
		else
			setApkParserPath(apkDir);
		if (null != callback)
			callback.initState(initState);
	}

	public boolean setApkParserPath(final String apkDir)
	{
		try
		{
			empty();
			if (null != apkDir && null != FileUtil.isExists(apkDir))
			{
				this.apkDir = apkDir;
				return initState = true;
			}
		}
		catch (Exception e)
		{
			Logger.e("ApkParser setApkParserPath() " + e.toString());
		}
		return initState = false;
	}

	/** 析构 */
	public void destroy()
	{
		empty();
		context = null;
	}

	private void empty()
	{
		if (null != am)
			am.close();
		am = null;
		if (null != hmBms)
			hmBms.clear();
		hmBms = null;
		apkDir = null;
		initState = false;
	}

	/**
	 * 初始化
	 * 
	 * @param apkDir
	 *            APK的路径
	 */
	private void initAssets()
	{
		try
		{
			if (null == am)
			{
				if (null != (am = AssetManager.class.newInstance()))
					am.getClass().getMethod(ADDASSETPATH, String.class).invoke(am, apkDir);
			}
		}
		catch (Exception e)
		{
			Logger.e("ApkParser initAssets() " + e.toString());
		}
	}

	/** 初始化 */
	@SuppressWarnings ("deprecation")
	@SuppressLint ("NewApi")
	public HashMap<String, Bitmap> initDrawable()
	{
		try
		{
			if (null == hmBms)
			{
				PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(apkDir, PackageManager.GET_ACTIVITIES);
				if (null != pi && null != pi.packageName)
				{
					Class<?> localClass = (new DexClassLoader(apkDir, context.getApplicationInfo().dataDir, null, context.getClass().getClassLoader())).loadClass(pi.packageName + ".R$drawable");
					Resources resources = getPackageResource(context, apkDir);
					Field[] fields;
					if (null != resources && null != localClass && null != (fields = localClass.getDeclaredFields()) && fields.length > 0)
					{
						Field field = null;
						int tempInt0 = fields.length;
						if (null != (hmBms = new HashMap<String, Bitmap>(tempInt0)))
						{
							while (tempInt0 > 0)
							{
								tempInt0--;
								try
								{
									if (null != (field = fields[tempInt0]))
										hmBms.put(field.getName(), ((BitmapDrawable) resources.getDrawable( (field.getInt(new Object())))).getBitmap());
								}
								catch (Exception e)
								{
									Logger.e("ApkParser initDrawable() hmBms.put " + e.toString());
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.e("ApkParser initDrawable() " + e.toString());
		}
		return hmBms;
	}

	/**
	 * 获取图片
	 * 
	 * @param bitmapName
	 *            /assets/ 目录下相对路径到具体文件(带后缀)
	 * */
	public Bitmap getAssetsBitmap(final String bitmapName)
	{
		if (!initState || null == bitmapName || bitmapName.length() <= 0)
			return null;
		try
		{
			initAssets();
			if (null != am)
				return BitmapFactory.decodeStream(am.open(bitmapName));
		}
		catch (Exception e)
		{
			Logger.e("ApkParser getAssetsBitmap() " + e.toString());
		}
		return null;
	}

	/**
	 * 获取文件
	 * 
	 * @param fileName
	 *            文件名称加后缀
	 * */
	public InputStream getAssetsFileContent(final String fileName)
	{
		if (!initState || null == fileName || fileName.length() <= 0)
			return null;
		try
		{
			initAssets();
			if (null != am)
				return am.open(fileName);
		}
		catch (Exception e)
		{
			Logger.e("ApkParser getAssetsFileContent() " + e.toString());
		}
		return null;
	}

	/**
	 * 获取图片
	 * 
	 * @param bitmapName
	 *            图片名称(不加后缀)
	 * */
	public Bitmap getDrawableBitmap(final String bitmapName)
	{
		if (!initState || null == bitmapName || bitmapName.length() <= 0)
			return null;
		try
		{
			HashMap<String, Bitmap> tempHmBms = initDrawable();
			if (null != tempHmBms)
				return tempHmBms.get(bitmapName);
		}
		catch (Exception e)
		{
			Logger.e("ApkParser getDrawableBitmap() " + e.toString());
		}
		return null;
	}

	private Resources getPackageResource(final Context context, final String packagePath)
	{
		try
		{
			// 反射出资源管理器
			Class<?> class_AssetManager = Class.forName(ASSETSMANAGERPATH);
			Object assetMag = class_AssetManager.newInstance();
			// 申明方法,由于addAssetPath()是隐藏的，所以只能通过反射调用
			Method method_addAssetPath = class_AssetManager.getDeclaredMethod(ADDASSETPATH, String.class);
			method_addAssetPath.invoke(assetMag, packagePath);
			Resources res = context.getResources();
			Constructor<?> constructor_Resources = Resources.class.getConstructor(class_AssetManager, res.getDisplayMetrics().getClass(), res.getConfiguration().getClass());
			res = (Resources) constructor_Resources.newInstance(assetMag, res.getDisplayMetrics(), res.getConfiguration());
			return res;
		}
		catch (Exception e)
		{
			Logger.e("ApkParser getPackageResource() " + e.toString());
		}
		return null;
	}
}
