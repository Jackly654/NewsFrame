package hz.dodo;

import hz.dodo.controls.PopError;
import android.content.Context;
import android.widget.Toast;

public class Logger
{
    private static final boolean DEBUG = false;
    
    public static final String tag = "LOG";
    
    public static int erc;
    public static String ers;
    
//    private static String head;
//    private static String pkg;
    private static boolean showError;
    private static Context ctx;

    public static void init(Context ctx, boolean showException)
    {
    	Logger.ctx = ctx;
    	
//    	pkg = "pkg=" + ctx.getPackageName();
//    	head = "model=" + Build.MODEL + "&release=" + android.os.Build.VERSION.RELEASE + "&vn=" + SystemUtil.getVName(ctx, ctx.getPackageName());
    	
    	showError = showException;
    	if(showError)
    	{
        	try{Toast.makeText(ctx, ctx.getPackageName() + "\n崩溃弹框显示", Toast.LENGTH_LONG).show();}
        	catch(Exception ext){}
    	}
    }
    
    public static void v(String msg)
    {
        v(tag, msg);
    }
    
    public static void v(String tag, String msg)
    { 
        if(DEBUG)
        { 
            android.util.Log.v(tag, msg);
//            write(msg);
        }
    }
    
    public static void v(String tag, String msg, Throwable tr) 
    { 
        if(DEBUG) 
        {
            android.util.Log.v(tag, msg, tr);
//            write(msg);
        } 
    }
    
    
    public static void d(String msg) 
    { 
        d(tag, msg);  
    }
    
    public static void d(String tag, String msg) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.d(tag, msg);
//            write(msg);
        } 
    }
    
    public static void d(String tag, String msg, Throwable tr) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.d(tag, msg, tr);
//            write(msg);
        } 
    }
    
    public static void i(String msg) 
    { 
        i(tag, msg);
    }
    
    public static void i(String tag, String msg) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.i(tag, msg);
//            write(msg);
        }
    }
    
    public static void i(String tag, String msg, Throwable tr) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.i(tag, msg, tr);
//            write(msg);
        } 
    }
    
    
    public static void w(String msg) 
    { 
        w(tag, msg);
    }
    
    public static void w(String tag, String msg) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.w(tag, msg);
//            write(msg);
        } 
    }
    
    public static void w(String tag, String msg, Throwable tr) 
    { 
        if(DEBUG) 
        { 
            android.util.Log.w(tag, msg, tr);
//            write(msg);
        } 
    }
    
    /**
     * 本地错误日志
     * @param msg
     */
    public static void e(String msg) 
    { 
        e(tag, msg);
    }
    
    /**
     * 本地显示错误日志
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) 
    { 
        if(DEBUG)
        { 
            android.util.Log.e(tag, msg);
//            write(msg);
        }
        postLog(msg);
    } 
    
    /**
     * 本地显示错误日志
     * @param tag
     * @param msg
     * @param tr
     */
    public static void e(String tag, String msg, Throwable tr) 
    { 
        if(DEBUG)
        { 
            android.util.Log.e(tag, msg, tr);
//            write(msg);
        }
        postLog(msg);
    }
    
    /**
     * 上传错误日志并抛出本地异常
     * @param err
     * @param e
     */
    public static void rep_err(String err, Throwable e1)
    {
        // 上传错误信息拼串
        if (erc < 10)
        {
            erc ++;
            ers += err;
        }
        e(err);
        /*if(DEBUG && e1 != null)
        {
        	e1.printStackTrace();
        }*/
    }
    
    /**
     * 只上传错误日志
     * @param err
     */
    public static void rep_err(String err)
    {
        rep_err(err, null);
    }
    
    private static void postLog(final String log)
    {
//    	new RC_POST(null, "http://www.haodongdong.com/dodoerror/postError?" + pkg, head + "&error=" + log, "", "");
    	
		if(showError)
		{
    		new PopError(ctx, log);
		}
    }
//	private static void write(String src)
//	{
//		if(src == null) return;
//		
//		try
//		{
//			String abspath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log/log.txt";
//			
//			File dirfile = new File(abspath.substring(0, abspath.lastIndexOf("/")));
//			// 如果不存在
//			if (!dirfile.exists()) dirfile.mkdirs(); // 创建多级目录结构
//			
//			// 删除已存在的临时文件
//			File file = new File(abspath);
//			if(!file.exists())
//			{
//				file.createNewFile();
//			}
//			
//			src += "\n";
//			FileOutputStream filefos = new FileOutputStream(file, true);
//			filefos.write(src.getBytes());
//			filefos.close();
//		}
//		catch(Exception e1)
//		{
//		}
//	}
}
