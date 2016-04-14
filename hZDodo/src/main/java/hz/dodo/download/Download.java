package hz.dodo.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import hz.dodo.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/*<permission android:name="android.permission.ACCESS_DOWNLOAD_MANAGER"/>*/

//(1) 响应下载中通知栏点击
//点击下载中通知栏提示，系统会对下载的应用单独发送Action为DownloadManager.ACTION_NOTIFICATION_CLICKED广播。intent.getData为content://downloads/all_downloads/29669，最后一位为downloadId。
//如果同时下载多个应用，intent会包含DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS这个key，表示下载的的downloadId数组。这里设计到下载管理通知栏的显示机制，会在下一篇具体介绍。

public class Download
{
//	ContentObserver observer = new ContentObserver(Downloads.CONTENT_URI) // 监听数据库的变化
//	{
//		public long download(String url, String locPath, String fileName)
//		{
//		};
//	};
	
	Context ctx;
	public Download(Context ctx)
	{
		this.ctx = ctx;
	}
	
	@SuppressLint ("NewApi")
	public long download(final String url, final String locPath, final String title, final String description, final String fileName)
	{
		try
		{
			Uri uri = Uri.parse(url);
			Request request = new Request(uri);
			
	        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();   // 获取文件类型实例
	        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));   // 获取文件类型 
	        
	        request.setMimeType(mimeString);  // 制定下载文件类型 
	        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
	        
//	        request.setShowRunningNotification(true);
	        
	        // 如果后台下载的话,要该权限
//	        android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
//	        request.setShowRunningNotification(false);
	        
	        request.setVisibleInDownloadsUi(true);
	        
	        // 设置名称
	        request.setTitle(title != null ? title : fileName);
	        // 描述信息
	        request.setDescription(description != null ? description : "");

	        // 表示允许MediaScanner扫描到这个文件，默认不允许。
	        request.allowScanningByMediaScanner();
	        
	        // Request.VISIBILITY_VISIBLE：在下载进行的过程中，通知栏中会一直显示该下载的Notification，当下载完成时，该Notification会被移除，这是默认的参数值。
	        // Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED：在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，直到用户点击该 Notification或者消除该Notification。
	        // Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION：只有在下载完成后该Notification才会被显示。
	        // Request.VISIBILITY_HIDDEN：不显示该下载请求的Notification。如果要使用这个参数，需要在应用的清单文件中加上DOWNLOAD_WITHOUT_NOTIFICATION权限。
	        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	        
	        /**设置下载后文件存放的位置
	         * 如果sdcard不可用, 会下载到 /storage/sdcard0/Android/data/com.dodo.calendar/files/ + locPath
	         */
	        request.setDestinationInExternalFilesDir(ctx, /*Environment.DIRECTORY_DOWNLOADS*/locPath, fileName);  // 指定下载的目录里
	        
	        DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(Activity.DOWNLOAD_SERVICE);
	        
	        //Download Manager的remove方法可以用来取消一个准备进行的下载，中止一个正在进行的下载，或者删除一个已经完成的下载。
	        //remove方法接受若干个download 的ID作为参数，你可以设置一个或者几个你想要取消的下载的ID，如下代码段所示：
//	        downloadManager.remove(REFERENCE_1, REFERENCE_2, REFERENCE_3);
	        
	        return downloadManager.enqueue(request);
		}
		catch(Exception e1)
		{
			Logger.e("download " + e1.toString());
		}
		
		return -1;
	}
	
	// 外部调用的话 new thread
	public String getReallyFileName(final String url)
	{
		if (url == null || url.length() <= 0) return null;
		
		HttpURLConnection conn = null;
		try
		{
			URL myURL = new URL(url);
			conn = (HttpURLConnection) myURL.openConnection();
			conn.connect();
			conn.getResponseCode();
			URL absUrl = conn.getURL();// 获得真实URL
			Logger.i("absUrl:" + absUrl);
			
			// 打印输出服务器Header信息
			Map<String, List<String>> map = conn.getHeaderFields();
			for (String str : map.keySet())
			{
				if (str != null)
				{
					Logger.i("key:" + str + ", valuc:" + map.get(str));
				}
			}

			// 通过Content-Disposition获取文件名，这点跟服务器有关，需要灵活变通
			String filename = conn.getHeaderField("Content-Disposition"); 
			if (filename == null || filename.length() < 1)
			{
				filename = absUrl.getFile();
			}
			
			return filename;
		}
		catch (MalformedURLException e1)
		{
			Logger.i("getReallyFileName MalformedURLException " + e1.toString());
		}
		catch (IOException e1)
		{
			Logger.i("getReallyFileName IOException " + e1.toString());
		}
		finally
		{
			if (conn != null)
			{
				conn.disconnect();
				conn = null;
			}
		}

		return null;
	}
	
	@SuppressLint ("NewApi")
	public static int queryDownloadStatus(Context ctx, long id)
	{
		Cursor cursor = null;
		try
		{
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(id);
			//setFilterById(long… ids)根据下载id进行过滤
			//setFilterByStatus(int flags)根据下载状态进行过滤
			//setOnlyIncludeVisibleInDownloadsUi(boolean value)根据是否在download ui中可见进行过滤。
			//orderBy(String column, int direction)根据列进行排序，不过目前仅支持DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP和DownloadManager.COLUMN_TOTAL_SIZE_BYTES排序。
			
			DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(Activity.DOWNLOAD_SERVICE);
			cursor = downloadManager.query(query);
			if (cursor != null && cursor.moveToFirst())
			{
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				switch (status)
				{
					case DownloadManager.STATUS_PAUSED:
						Logger.i("STATUS_PAUSED");
					case DownloadManager.STATUS_PENDING:
						Logger.i("STATUS_PENDING");
					case DownloadManager.STATUS_RUNNING:
						Logger.i("STATUS_RUNNING");
						break;
					case DownloadManager.STATUS_SUCCESSFUL:
						Logger.i("下载完成");
						break;
					case DownloadManager.STATUS_FAILED:
						Logger.i("STATUS_FAILED");
						break;
				}
				return status;
			}
		}
		catch(Exception ect)
		{
			Logger.e("queryDownloadStatus() " + ect.toString());
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return DownloadManager.ERROR_UNKNOWN;
	}
	
	@SuppressLint ({ "InlinedApi", "NewApi" })
	public static String queryDownloadedPath(Context ctx, final long id)
	{
		Cursor cursor = null;
		try
		{
			Query query = new Query();
			query.setFilterById(id);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
			cursor = downloadManager.query(query);
			
			if(cursor != null && cursor.moveToFirst())
			{
				return cursor.getString(cursor.getColumnIndex("local_filename"));
			}
		}
		catch(Exception e1)
		{
			Logger.e("queryDownloadedPath() " + e1.toString());
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return null;
	}
}
