package hz.dodo;

import hz.dodo.data.Empty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

public class DLImg implements RC_GET.NetGetCallBack
{
	public static interface Callback
	{
		public void onImg(final boolean success, final String abspath, final Object obj);
		public boolean isConnect(); // 是否有网络连接
	}
	
	Callback
		callback;
	
	List<String>
		ltUrl; // 待下载的任务
	HashMap<String, String>
		hmLp; // 保存下载地址
	HashMap<String, Object>
		hmObj; // 给调用者返回的标记
	
	public DLImg(Context ctx, final Callback callback)
	{
		this.callback = callback;
		
		ltUrl = new ArrayList<String>(5);
		hmLp = new HashMap<String, String>(5);
		hmObj = new HashMap<String, Object>(5);
	}
	public void onDestroy()
	{
		ltUrl.clear();
		hmLp.clear();
		hmObj.clear();
	}
	// 正常请求,排队
	public void reqImg(final String url, final String localPath, final Object obj)
	{
		addQueue(url, localPath, -1, obj);
	}
	// 优先请求,插队
	public void insertImg(final String url, final String localPath, final Object obj)
	{
		addQueue(url, localPath, 0, obj);
	}
	// 当网络连接时通知
	public void onConnect()
	{
		if(SDCard.checkSdcard() && ltUrl != null && hmLp != null)
		{
			if(ltUrl.size() == 1)
			{
				String url = ltUrl.get(0);
				if(url != null)
				{
					request(url, hmLp.get(url));
				}
			}
		}
	}
	private boolean isReq()
	{
		return callback == null ? false : callback.isConnect();
	}
	private void addQueue(final String url, final String localPath, final int index, final Object obj)
	{
		addData(url, localPath, index, obj);
		if(ltUrl.size() == 1)
		{
			request(url, localPath);
		}
	}
	private void addData(final String url, final String localPath, final int index, final Object obj)
	{
		if(Empty.isEmpty(url) || Empty.isEmpty(localPath) || ltUrl.contains(url)) return;
		
		if(FileUtil.isExists(localPath) == null)
		{
			switch(index)
			{
				case 0:
					ltUrl.add(0 , url);
					break;
				default:
					ltUrl.add(url);
					break;
			}
			hmLp.put(url, localPath);
			if(obj != null)
			{
				hmObj.put(url, obj);
			}
		}
	}
	private void removeData(final String url)
	{
		if(Empty.isEmpty(url)) return;
		ltUrl.remove(url);
		hmLp.remove(url);
		hmObj.remove(url);
	}
	private void request(final String url, final String localPath)
	{
		if(!isReq() || !SDCard.checkSdcard() || Empty.isEmpty(url)) return;
		new RC_GET(this, url, "REQ_IMG", url);
	}
	private void downloadImg(InputStream is, int size, String tag1, String tag2)
	{
		if(is != null)
		{
			if(SDCard.checkSdcard())
			{
				String path = hmLp.get("" + tag2);
				if(path != null)
				{
					boolean bRst = true;
					FileUtil fu = new FileUtil();
					if(fu.write(is, size, path) == FileUtil.rst_success)
					{
					}
					else
					{
						bRst = false;
					}
					if(callback != null)
					{
						callback.onImg(bRst, path, hmObj.get(tag1));
					}
					fu = null;
				}
			}
		}
		
		// 清除已下载或失败
		removeData(tag2);

		// 继续其他任务
		if(ltUrl.size() > 0)
		{
			String url = ltUrl.get(0);
			request(url, hmLp.get(url));
		}
	}
	@Override
	public void recGetRes(InputStream is, int size, String tag1, String tag2)
	{
		if("REQ_IMG".equals("" + tag1))
		{
			downloadImg(is, size, tag1, tag2);
		}
	}
	@Override
	public void recGetErr(String code, String tag1, String tag2)
	{
		if("REQ_IMG".equals("" + tag1))
		{
			downloadImg(null, 0, tag1, tag2);
		}
	}
}
