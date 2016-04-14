package hz.dodo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

public class SDCard
{
	private static String sdroot, sd_cid;
	
	// 获取扩展SD卡设备状态
	public static boolean checkSdcard()
	{
		String sdState = Environment.getExternalStorageState();
		if (!sdState.equals(Environment.MEDIA_MOUNTED) && !sdState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		{
			return false;
		}
		return true;
	}
	
	@SuppressLint("NewApi")
	public static String getSDCardRootPath(Context ctx)
	{
		if(!checkSdcard()) return null;
		
		if(sdroot != null && sdroot.length() > 0) return sdroot;
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		if(android.os.Build.VERSION.SDK_INT <= 10) // 2.3以下 android.os.Build.VERSION_CODES.GINGERBREAD;
		{
//			if(Environment.isExternalStorageRemovable()) // 可卸载
			{
				sdroot = path;//Environment.getExternalStorageDirectory().getAbsolutePath();
				return sdroot;
			}
		}
		else
		{
			StorageManager sm = (StorageManager)ctx.getSystemService(Context.STORAGE_SERVICE); 
            try
            {
//            	String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            	/*if(Environment.isExternalStorageEmulated()) // 是否为模拟
            	{
            		System.out.println("模拟");
            	}*/
            	
            	if(Environment.isExternalStorageRemovable()) // 可卸载
            	{
        			if(createFile(path + "/.hdd")) return sdroot = path;
        			return null;
            	}
            	else
            	{
            		Method method_path = sm.getClass().getMethod("getVolumePaths");
                	
                	String[] paths = (String[]) method_path.invoke(sm);
                	String str;
                	
                	int i1 = 0;
                	while(i1 < paths.length)
                	{
                		if(null != (str = paths[i1]))
                		{
                			str = str.toLowerCase(Locale.ENGLISH);
                			if(str.equals(path) && str.contains("sdcard"))
                			{
                				if(createFile(str + "/.hdd")) return sdroot = str;
                				return null;
                			}
                		}
                		i1++;
                	}
                	
                	return sdroot = path;
            	}
            }
            catch (Exception e1)
            {
            	System.out.println("error: " + e1.toString());
            } 
		}
		return null;
	}
	
	// 严格返回可插拔SD卡路径,文件管理器用这个方法,其他程序用 getSDCardRootPath()
	@SuppressLint ("NewApi")
	public static String getSDCardRootPath2(Context ctx)
	{
		if(!checkSdcard()) return null;
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		if(android.os.Build.VERSION.SDK_INT <= 10) // 2.3以下 android.os.Build.VERSION_CODES.GINGERBREAD;
		{
			return path;
		}
		else
		{
			StorageManager sm = (StorageManager)ctx.getSystemService(Context.STORAGE_SERVICE); 
            try
            {
            	if(Environment.isExternalStorageRemovable() && Environment.MEDIA_MOUNTED.equals("" + getStatus(sm, path))) // 可卸载且挂载
            	{
        			return path;
            	}
            	else
            	{
            		Method method_path = sm.getClass().getMethod("getVolumePaths");
                	Logger.i("-----------------------");
                	String[] paths = (String[]) method_path.invoke(sm);
                	String str;
                	
//                	int i1 = paths.length - 1;
                	int i1 = 1;
                	while(i1 < paths.length)
                	{
                		if(null != (str = paths[i1]))
        				{
                    		String status = getStatus(sm, str);
                    		Logger.i("path:" + paths[i1] + ", status:" + status);
                    		if(Environment.MEDIA_MOUNTED.equals("" + status))
                    		{
//                    			if(i1  > 0)
                    			{
                    				Logger.d("返回已挂载的路径");
                    				return str;
                    			}
                    		}
                    		else if(Environment.MEDIA_UNMOUNTED.equals("" + status) || Environment.MEDIA_BAD_REMOVAL.equals("" + status))
                    		{
                    			// 已经卸载,该路径一定是SD卡路径
                    			Logger.i("已经卸载,该路径一定是SD卡路径");
                    			return null;
                    		}
        				}
                		++i1;
                	}
                	
                	Logger.i("-----------------------");
            	}
            }
            catch (Exception e1)
            {
            	Logger.e("getSDCardRootPath2() " + e1.toString());
            }
		}
		return null;
	}

	// 返回一组存储路径
	@SuppressLint ("InlinedApi")
	public static List<String> getMemoryPaths(Context ctx)
	{
		StorageManager sm = (StorageManager)ctx.getSystemService(Context.STORAGE_SERVICE); 
        try
        {
    		Method method_path = sm.getClass().getMethod("getVolumePaths");
        	Logger.i("-----------------------");
        	String[] paths = (String[]) method_path.invoke(sm);
        	List<String> lt = new ArrayList<String>(1);
        	String str;
        	
        	int i1 = paths.length - 1;
        	while(i1 >= 0)
        	{
        		if(null != (str = paths[i1]))
				{
            		String status = getStatus(sm, str);
            		Logger.i("path:" + paths[i1] + ", status:" + status);
            		if(Environment.MEDIA_MOUNTED.equals("" + status))
            		{
            			lt.add(str);
            		}
				}
        		--i1;
        	}
        	
        	Logger.i("-----------------------");
        	return lt;
        }
        catch (Exception e1)
        {
        	Logger.e("getMemory() " + e1.toString());
        }
        return null;
	}
	
	public static String getStatus(StorageManager sm, String path)
	{
		try
		{
			return (String) sm.getClass().getMethod("getVolumeState", String.class).invoke(sm, path);
		}
		catch(Exception e1)
		{
			Logger.e("getStatus() " + e1.toString());
		}
		return "error";
	}
	
	private static boolean createFile(final String path)
	{
		try
		{
			File file = new File(path);
			if(file.exists()) return true;
			
			if(file.createNewFile()) return true;
		}
		catch(Exception e1)
		{
			System.out.println("createNewFile error: " + e1.toString());
		}
		
		return false;
	}
	
	public static String getSDCid()
	{
		if (sd_cid != null) return sd_cid;

		String str1 = null;
		BufferedReader bfr = null;
		Object obj = null;
		try
		{
			bfr = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/type"));
			obj = bfr.readLine().toLowerCase(Locale.getDefault()).contentEquals("sd");
			if(obj != null)
			{
				str1 = "/sys/block/mmcblk0/device/";
			}
			bfr.close();
			bfr = null;
		}
		catch (Exception e1)
		{
		}
		try
		{
			bfr = new BufferedReader(new FileReader("/sys/block/mmcblk1/device/type"));
			obj = bfr.readLine().toLowerCase(Locale.getDefault()).contentEquals("sd");
			if(obj != null)
			{
				str1 = "/sys/block/mmcblk1/device/";
			}
			bfr.close();
			bfr = null;
		}
		catch (Exception e1)
		{
		}
		try
		{
			bfr = new BufferedReader(new FileReader("/sys/block/mmcblk2/device/type"));
			obj = bfr.readLine().toLowerCase(Locale.getDefault()).contentEquals("sd");
			if(obj != null)
			{
				str1 = "/sys/block/mmcblk2/device/";
			}
			bfr.close();
			bfr = null;
		}
		catch (Exception e1)
		{
		}
		try
		{
			if(str1 != null)
			{
				bfr = new BufferedReader(new FileReader(str1 + "cid"));
				sd_cid = bfr.readLine();
				bfr.close();
				return sd_cid;
			}
		}
		catch (Exception e1)
		{
		}
		return "";
	}
	
	// test
	/*private static void write(final String path)
	{
		try
		{
			File file = new File(path);
			file.createNewFile();
			
			String str = "gong chan dang wai sui";
			
			FileOutputStream filefos = new FileOutputStream(file, true);
			int i1 = 10000;
			while(i1 >= 0)
			{
				filefos.write(str.getBytes(), 0, str.getBytes().length);
				i1--;
			}
			filefos.close();
		}
		catch(Exception e1)
		{
			System.out.println();
			return;
		}
	}*/
	
//	@SuppressLint ("NewApi")
//	public static void test3(Context ctx)
//	{
//
//		if(!checkSdcard()) return;
//		
//		if(android.os.Build.VERSION.SDK_INT <= 10) // 2.3以下
//		{
//			if(Environment.isExternalStorageRemovable()) // 可卸载
//			{
////				return Environment.getExternalStorageDirectory().getAbsolutePath();
//			}
//		}
//		else
//		{
//			StorageManager sm = (StorageManager)ctx.getSystemService(Context.STORAGE_SERVICE); 
//            try
//            {
//            	String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//            	/*if(Environment.isExternalStorageEmulated()) // 是否为模拟
//            	{
//            		System.out.println("模拟");
//            	}*/
//            	
//            	if(Environment.isExternalStorageRemovable()) // 可卸载
//            	{
//        			Logger.d("可卸载:" + path);
//            	}
//            	else
//            	{
//            		Method method_path = sm.getClass().getMethod("getVolumePaths");
//                	
//                	String[] paths = (String[]) method_path.invoke(sm);
//                	
//                	if(paths != null && paths.length > 0)
//                	{
//                    	int i1 = 0;
//                    	while(i1 < paths.length)
//                    	{
//                    		Logger.d("路径:" + i1 + " - " + paths[i1]);
//                    		
//                    		if(!paths[i1].equals(path) && paths[i1].toLowerCase().contains("sdcard"))
//                    		{
//                    			Logger.d("判断为SD卡路径=" + paths[i1]);
//                    		}
//                    		i1++;
//                    	}
//                	}
//                	else
//                	{
//                		Logger.d("路径只能是=" + path);
//                	}
//            	}
//            }
//            catch (Exception e1)
//            {
//            	System.out.println("error: " + e1.toString());
//            } 
//		}
//	
//	}
//	
//	// 也不行
//	public static void test2(Context ctx)
//	{
//		try
//		{
//			StorageManager sm = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
//			// 获取sdcard的路径：外置和内置
//			String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
//			String esd = Environment.getExternalStorageDirectory().getPath();
//			for (int i = 0; i < paths.length; i++)
//			{
//				Logger.i("路径" + i + " = " + paths[i]);
//			    if (paths[i].equals(esd))
//			    {
//			        continue;
//			    }
//			    File sdFile = new File(paths[i]);
//			    if (sdFile.canWrite())
//			    {
//			    	Logger.i("SD卡路径 = " + paths[i]);
//			    }
//			}
//		}
//		catch(Exception e1)
//		{
//			Logger.e("" + e1.toString());
//		}
//	}
//	public static void test1()
//	{
//		String s = "";
//		try
//		{
//			Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
//			process.waitFor();
//
//			InputStream is = process.getInputStream();
//			byte[] buffer = new byte[1024];
//			while (is.read(buffer) != -1)
//			{
//				s = s + new String(buffer);
//			}
//			is.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//		// 用行分隔mount列表
//		String[] lines = s.split("\n");
//		for (int i = 0; i < lines.length; i++)
//		{
//			// 如果行内有挂载路径且为vfat类型，说明可能是内置或者外置sd的挂载点
////			if (-1 != lines[i].indexOf(path[0]) && -1 != lines[i].indexOf("vfat")) // 原来
//			if (-1 != lines[i].indexOf("vfat"))
//			{
//				// 再用空格分隔
//				String[] blocks = lines[i].split("\\s");
//				for (int j = 0; j < blocks.length; j++)
//				{
//					// 判断是否是挂载为vfat类型
////					if (-1 != blocks[j].indexOf(path[0])) // 原来
//					if (-1 != blocks[j].indexOf("vfat"))
//					{
//						// Test if it is the external sd card.
//						Logger.d(blocks[j-1]);
//					}
//				}
//			}
//		}
//	}
}
