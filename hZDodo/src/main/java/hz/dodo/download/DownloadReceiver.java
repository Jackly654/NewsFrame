package hz.dodo.download;

import hz.dodo.Logger;
import hz.dodo.PkgMng;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class DownloadReceiver extends BroadcastReceiver
{
	@SuppressLint ("NewApi")
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			String action = intent.getAction();
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
			{
				Logger.i("下载完成 或 取消");

				// 下载完成的ID
				long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

				Query query = new Query();
				query.setFilterById(id);
				query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
				DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor cursor = downloadManager.query(query);
				
				// -------------------
				if(cursor != null && cursor.moveToFirst())
				{
//					media_type: application/vnd.android.package-archive
//					local_filename: /storage/emulated/0/Android/data/com.dodo.download/files/apks/***.apk
					
					// 如果是APK,调起安装
					String media_type = cursor.getString(cursor.getColumnIndex("media_type"));
					if("application/vnd.android.package-archive".equals(media_type))
					{
						String local_filename = cursor.getString(cursor.getColumnIndex("local_filename"));
						PkgMng.install(context, local_filename);
					}
				}
				cursor.close();
				// -------------------

//				int columnCount = cursor.getColumnCount();
//				String path = null;

				// 这里把所有的列都打印一下，有什么需求，就怎么处理,文件的本地路径就是path
//				while (cursor.moveToNext())
//				{
//					for (int j = 0; j < columnCount; j++)
//					{
//						String columnName = cursor.getColumnName(j);
//						String string = cursor.getString(j);
//						if (columnName.equals("local_uri"))
//						{
//							path = string;
//						}
//						if (string != null)
//						{
//							Logger.i(columnName + ": " + string);
//						}
//						else
//						{
//							Logger.i(columnName + ": null");
//						}
//					}
//				}
//				cursor.close();
//
//				// 如果sdcard不可用时下载下来的文件，那么这里将是一个内容提供者的路径，这里打印出来，有什么需求就怎么样处理
//				if (path != null && path.startsWith("content:"))
//				{
//					cursor = context.getContentResolver().query(Uri.parse(path), null, null, null, null);
//					columnCount = cursor.getColumnCount();
//					while (cursor.moveToNext())
//					{
//						for (int j = 0; j < columnCount; j++)
//						{
//							String columnName = cursor.getColumnName(j);
//							String string = cursor.getString(j);
//							if (string != null)
//							{
//								Logger.i(columnName + ": " + string);
//							}
//							else
//							{
//								Logger.i(columnName + ": null");
//							}
//						}
//					}
//					cursor.close();
//				}
			}
			else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action))
			{
				Logger.i("用户点击了下载的Notification");
				PkgMng.startDownloadUI(context);
			}
			else if(DownloadManager.ACTION_VIEW_DOWNLOADS.equals(action))
			{
				Logger.i("ACTION_VIEW_DOWNLOADS");
			}
		}
		catch(Exception e1)
		{
			Logger.e("DownloadReceiver onReceiver " + e1.toString());
		}
	}
}
